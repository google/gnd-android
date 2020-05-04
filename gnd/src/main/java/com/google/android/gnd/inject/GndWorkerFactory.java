/*
 * Copyright 2019 Google LLC
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

package com.google.android.gnd.inject;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;
import com.google.android.gnd.persistence.local.LocalDataStore;
import com.google.android.gnd.persistence.remote.RemoteDataStore;
import com.google.android.gnd.persistence.remote.RemoteStorageManager;
import com.google.android.gnd.persistence.sync.LocalMutationSyncWorker;
import com.google.android.gnd.persistence.sync.PhotoSyncWorker;
import com.google.android.gnd.persistence.sync.TileDownloadWorker;
import com.google.android.gnd.system.NotificationManager;
import javax.inject.Inject;

/** Custom {@code WorkerFactory} to allow Dagger 2 injection into Ground Workers. */
public class GndWorkerFactory extends WorkerFactory {
  // TODO(github.com/google/dagger/issues/1183): Remove this factory once direct injection
  // supported on Workers.
  private final LocalDataStore localDataStore;
  private final RemoteDataStore remoteDataStore;
  private final RemoteStorageManager remoteStorageManager;
  private final NotificationManager notificationManager;

  @Inject
  public GndWorkerFactory(
      LocalDataStore localDataStore,
      RemoteDataStore remoteDataStore,
      RemoteStorageManager remoteStorageManager,
      NotificationManager notificationManager) {
    this.localDataStore = localDataStore;
    this.remoteDataStore = remoteDataStore;
    this.remoteStorageManager = remoteStorageManager;
    this.notificationManager = notificationManager;
  }

  @Nullable
  @Override
  public ListenableWorker createWorker(
      @NonNull Context appContext,
      @NonNull String workerClassName,
      @NonNull WorkerParameters params) {
    // There are more generic and robust ways of doing this, but individual constructors are
    // hard-coded for simplicity. If and when github.com/google/dagger/issues/1183 is implemented
    // this class can be removed in favor of DI.
    if (workerClassName.equals(LocalMutationSyncWorker.class.getName())) {
      return new LocalMutationSyncWorker(
          appContext, params, localDataStore, remoteDataStore, notificationManager);
    } else if (workerClassName.equals(TileDownloadWorker.class.getName())) {
      return new TileDownloadWorker(appContext, params, localDataStore, notificationManager);
    } else if (workerClassName.equals(PhotoSyncWorker.class.getName())) {
      return new PhotoSyncWorker(appContext, params, remoteStorageManager, notificationManager);
    } else {
      throw new IllegalArgumentException("Unknown worker class " + workerClassName);
    }
  }
}
