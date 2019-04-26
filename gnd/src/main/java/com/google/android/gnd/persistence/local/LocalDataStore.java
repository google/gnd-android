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

package com.google.android.gnd.persistence.local;

import com.google.android.gnd.persistence.remote.RemoteChange;
import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface LocalDataStore {
  Single<Long> applyAndEnqueue(LocalChange localChange);

  ImmutableList<LocalChange> getPendingChanges(String featureId);

  void dequeue(ImmutableList<LocalChange> localChanges);

  void markFailed(ImmutableList<LocalChange> localChanges);

  Completable merge(RemoteChange<?> remoteChange);
}
