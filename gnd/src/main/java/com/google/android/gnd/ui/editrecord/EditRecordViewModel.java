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

package com.google.android.gnd.ui.editrecord;

import static com.google.android.gnd.util.Streams.toImmutableList;
import static com.google.android.gnd.vo.FeatureUpdate.Operation.CREATE;
import static com.google.android.gnd.vo.FeatureUpdate.Operation.DELETE;
import static com.google.android.gnd.vo.FeatureUpdate.Operation.UPDATE;
import static java8.util.Maps.forEach;
import static java8.util.stream.StreamSupport.stream;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableMap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gnd.GndApplication;
import com.google.android.gnd.R;
import com.google.android.gnd.repository.DataRepository;
import com.google.android.gnd.repository.Resource;
import com.google.android.gnd.system.AuthenticationManager;
import com.google.android.gnd.ui.common.AbstractViewModel;
import com.google.android.gnd.ui.common.SingleLiveEvent;
import com.google.android.gnd.vo.FeatureUpdate.RecordUpdate.ResponseUpdate;
import com.google.android.gnd.vo.Form.Element.Type;
import com.google.android.gnd.vo.Form.Field;
import com.google.android.gnd.vo.Record;
import com.google.android.gnd.vo.Record.Response;
import com.google.android.gnd.vo.Record.TextResponse;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.BehaviorProcessor;
import java8.util.Optional;
import java8.util.stream.Stream;
import javax.inject.Inject;

// TODO: Save draft to local db on each change.
public class EditRecordViewModel extends AbstractViewModel {
  private static final String TAG = EditRecordViewModel.class.getSimpleName();

  private final DataRepository dataRepository;
  private final AuthenticationManager authManager;
  private final MutableLiveData<Resource<Record>> record;
  private final SingleLiveEvent<Void> showUnsavedChangesDialogEvents;
  private final SingleLiveEvent<Void> showErrorDialogEvents;
  private final Resources resources;
  private final ObservableMap<String, Response> responses = new ObservableArrayMap<>();
  private final ObservableMap<String, String> errors = new ObservableArrayMap<>();
  private final BehaviorProcessor<EditRecordFragmentArgs> argumentProcessor;
  private final BehaviorProcessor<Record> recordProcessor;

  public final ObservableInt loadingSpinnerVisibility = new ObservableInt();

  @Inject
  EditRecordViewModel(
      GndApplication application,
      DataRepository dataRepository,
      AuthenticationManager authenticationManager) {
    this.resources = application.getResources();
    this.dataRepository = dataRepository;
    this.record = new MutableLiveData<>();
    this.showUnsavedChangesDialogEvents = new SingleLiveEvent<>();
    this.showErrorDialogEvents = new SingleLiveEvent<>();
    this.authManager = authenticationManager;
    this.argumentProcessor = BehaviorProcessor.create();
    this.recordProcessor = BehaviorProcessor.create();

    Flowable<Resource<Record>> recordStream =
        recordProcessor.flatMap(
            r ->
                this.authManager
                    .getUser()
                    .flatMap(user -> this.dataRepository.saveChanges(r, getChangeList(r), user))
                    .toFlowable(BackpressureStrategy.LATEST));

    Single<Resource<Record>> newRecord = argumentProcessor.map()

    disposeOnClear(recordStream.subscribe(record::setValue));
  }

  public ObservableMap<String, Response> getResponses() {
    return responses;
  }

  public Optional<Response> getResponse(String fieldId) {
    return Optional.ofNullable(responses.get(fieldId));
  }

  public ObservableMap<String, String> getErrors() {
    return errors;
  }

  public void onTextChanged(Field field, String text) {
    Log.v(TAG, "onTextChanged: " + field.getId());

    onResponseChanged(field, TextResponse.fromString(text));
  }

  public void onResponseChanged(Field field, Optional<Response> newResponse) {
    Log.v(
        TAG, "onResponseChanged: " + field.getId() + " = '" + Response.toString(newResponse) + "'");
    newResponse.ifPresentOrElse(
        r -> responses.put(field.getId(), r), () -> responses.remove(field.getId()));
    updateError(field, newResponse);
  }

  public void onFocusChange(Field field, boolean hasFocus) {
    if (!hasFocus) {
      updateError(field);
    }
  }

  LiveData<Resource<Record>> getRecord() {
    return record;
  }

  LiveData<Void> getShowUnsavedChangesDialogEvents() {
    return showUnsavedChangesDialogEvents;
  }

  public LiveData<Void> getShowErrorDialogEvents() {
    return showErrorDialogEvents;
  }

  void editNewRecord(String projectId, String featureId, String formId) {
    // TODO(#24): Fix leaky subscriptions!

    argumentProcessor.onNext(args);

    disposeOnClear(
        dataRepository
            .createRecord(projectId, featureId, formId)
            .map(Resource::loaded)
            .doOnSuccess(__ -> onNewRecordLoaded())
            .subscribe(this::onRecordSnapshot));
  }

  @NonNull
  private Optional<Record> getCurrentRecord() {
    return Resource.getData(record);
  }

  private void onNewRecordLoaded() {
    responses.clear();
    errors.clear();
  }

  private void updateMap(Record r) {
    Log.v(TAG, "Updating map");
    responses.clear();
    forEach(
        r.getResponseMap(),
        (k, v) ->
            r.getForm().getField(k).ifPresent(field -> onResponseChanged(field, Optional.of(v))));
  }

  void editExistingRecord(String projectId, String featureId, String recordId) {
    // TODO: Store and retrieve latest edits from cache and/or db.
    // TODO(#24): Fix leaky subscriptions!
    disposeOnClear(
        dataRepository
            .getRecordSnapshot(projectId, featureId, recordId)
            .doOnSuccess(r -> r.data().ifPresent(this::update))
            .subscribe(this::onRecordSnapshot));
  }

  private void onRecordSnapshot(Resource<Record> r) {
    switch (r.operationState().get()) {
      case LOADING:
        loadingSpinnerVisibility.set(View.VISIBLE);
        break;
      case LOADED:
        loadingSpinnerVisibility.set(View.GONE);
        break;
      case SAVING:
        break;
      case SAVED:
        break;
      case NOT_FOUND:
      case ERROR:
        break;
    }
    // TODO: Replace with functional stream.
    record.setValue(r);
  }

  boolean onSaveClick() {
    getCurrentRecord().ifPresent(this::updateErrors);
    if (hasErrors()) {
      showErrorDialogEvents.setValue(null);
      return true;
    }
    if (hasUnsavedChanges()) {
      saveChanges();
      return true;
    }
    return false;
  }

  boolean onBack() {
    if (hasUnsavedChanges()) {
      showUnsavedChangesDialogEvents.setValue(null);
      return true;
    } else {
      return false;
    }
  }

  private void saveChanges() {
    getCurrentRecord().ifPresent(this::saveChanges);
  }

  private void saveChanges(Record r) {
    recordProcessor.onNext(r);
  }

  private Stream<ResponseUpdate> getChanges(Record r) {
    return stream(r.getForm().getElements())
        .filter(e -> e.getType() == Type.FIELD)
        .map(e -> e.getField())
        .flatMap(f -> getChanges(r, f));
  }

  private ImmutableList<ResponseUpdate> getChangeList(Record r) {
    return getChanges(r).collect(toImmutableList());
  }

  private Stream<ResponseUpdate> getChanges(Record record, Field field) {
    String fieldId = field.getId();
    Optional<Response> originalResponse = record.getResponse(fieldId);
    Optional<Response> currentResponse = getResponse(fieldId).filter(r -> !r.isEmpty());
    if (currentResponse.equals(originalResponse)) {
      return stream(Collections.emptyList());
    }

    ResponseUpdate.Builder update = ResponseUpdate.newBuilder();
    update.setElementId(fieldId);
    if (!currentResponse.isPresent()) {
      update.setOperation(DELETE);
    } else if (originalResponse.isPresent()) {
      update.setOperation(UPDATE);
      update.setResponse(currentResponse);
    } else {
      update.setOperation(CREATE);
      update.setResponse(currentResponse);
    }
    return stream(Arrays.asList(update.build()));
  }

  private void update(Record record) {
    updateMap(record);
    updateErrors(record);
  }

  private void updateErrors(Record r) {
    errors.clear();
    stream(r.getForm().getElements())
        .filter(e -> e.getType().equals(Type.FIELD))
        .map(e -> e.getField())
        .forEach(this::updateError);
  }

  private void updateError(Field field) {
    updateError(field, getResponse(field.getId()));
  }

  private void updateError(Field field, Optional<Response> response) {
    String key = field.getId();
    if (field.isRequired() && !response.filter(r -> !r.isEmpty()).isPresent()) {
      Log.d(TAG, "Missing: " + key);
      errors.put(field.getId(), resources.getString(R.string.required_field));
    } else {
      Log.d(TAG, "Valid: " + key);
      errors.remove(field.getId());
    }
  }

  private boolean hasUnsavedChanges() {
    return getCurrentRecord().map(r -> getChanges(r).findAny().isPresent()).orElse(false);
  }

  private boolean hasErrors() {
    return !errors.isEmpty();
  }
}
