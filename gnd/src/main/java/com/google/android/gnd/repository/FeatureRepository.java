/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gnd.repository;

import com.google.android.gnd.model.Mutation;
import com.google.android.gnd.model.Project;
import com.google.android.gnd.model.User;
import com.google.android.gnd.model.feature.Feature;
import com.google.android.gnd.model.feature.FeatureMutation;
import com.google.android.gnd.persistence.local.LocalDataStore;
import com.google.android.gnd.persistence.remote.NotFoundException;
import com.google.android.gnd.persistence.remote.RemoteDataEvent;
import com.google.android.gnd.persistence.remote.RemoteDataStore;
import com.google.android.gnd.persistence.sync.DataSyncWorkManager;
import com.google.android.gnd.rx.ValueOrError;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import java.util.Date;
import java8.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import timber.log.Timber;

/**
 * Coordinates persistence and retrieval of {@link Feature} instances from remote, local, and in
 * memory data stores. For more details on this pattern and overall architecture, see
 * https://developer.android.com/jetpack/docs/guide.
 */
@Singleton
public class FeatureRepository {

  private final LocalDataStore localDataStore;
  private final RemoteDataStore remoteDataStore;
  private final ProjectRepository projectRepository;
  private final DataSyncWorkManager dataSyncWorkManager;

  @Inject
  public FeatureRepository(
      LocalDataStore localDataStore,
      RemoteDataStore remoteDataStore,
      ProjectRepository projectRepository,
      DataSyncWorkManager dataSyncWorkManager) {
    this.localDataStore = localDataStore;
    this.remoteDataStore = remoteDataStore;
    this.projectRepository = projectRepository;
    this.dataSyncWorkManager = dataSyncWorkManager;
  }

  /**
   * Mirrors features in the specified project from the remote db into the local db when the network
   * is available. When invoked, will first attempt to resync all features from the remote db,
   * subsequently syncing only remote changes. The returned stream never completes, and
   * subscriptions will only terminate on disposal.
   */
  public Completable syncFeatures(Project project) {
    return remoteDataStore
        .loadFeaturesOnceAndStreamChanges(project)
        .switchMapCompletable(this::updateLocalFeature);
  }

  // TODO: Remove "feature" qualifier from this and other repository method names.
  private Completable updateLocalFeature(RemoteDataEvent<Feature> event) {
    switch (event.getEventType()) {
      case ENTITY_LOADED:
      case ENTITY_MODIFIED:
        return event.value().map(localDataStore::mergeFeature).orElse(Completable.complete());
      case ENTITY_REMOVED:
        // TODO: Delete features:
        // localDataStore.removeFeature(event.getEntityId());
        return Completable.complete();
      case ERROR:
        event.error().ifPresent(e -> Timber.d(e, "Invalid features in remote db ignored"));
        return Completable.complete();
      default:
        return Completable.error(
            new UnsupportedOperationException("Event type: " + event.getEventType()));
    }
  }

  // TODO: Only return feature fields needed to render features on map.
  // TODO(#127): Decouple from Project and accept id instead.
  public Flowable<ImmutableSet<Feature>> getFeaturesOnceAndStream(Project project) {
    return localDataStore.getFeaturesOnceAndStream(project);
  }

  // TODO(#127): Decouple Project from Feature and remove projectId.
  // TODO: Replace with Single and treat missing feature as error.
  // TODO: Don't require projectId to be the active project.
  public Maybe<Feature> getFeature(String projectId, String featureId) {
    return projectRepository
        .getActiveProjectOnceAndStream()
        .map(ValueOrError::value)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(project -> project.getId().equals(projectId))
        .switchIfEmpty(Observable.error(() -> new NotFoundException("Project " + projectId)))
        .flatMapMaybe(project -> localDataStore.getFeature(project, featureId))
        .firstElement();
  }

  public Completable saveFeature(Feature feature, User user) {
    // TODO(#80): Update UI to provide FeatureMutations instead of Features here.
    return localDataStore
        .applyAndEnqueue(
            FeatureMutation.builder()
                .setType(Mutation.Type.CREATE)
                .setProjectId(feature.getProject().getId())
                .setFeatureId(feature.getId())
                .setLayerId(feature.getLayer().getId())
                .setNewLocation(Optional.of(feature.getPoint()))
                .setUserId(user.getId())
                .setClientTimestamp(new Date())
                .build())
        .andThen(dataSyncWorkManager.enqueueSyncWorker(feature.getId()));
  }
}
