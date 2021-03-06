/*
 * Copyright 2021 Google LLC
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

import com.google.android.gnd.model.TermsOfService;
import com.google.android.gnd.persistence.local.LocalValueStore;
import com.google.android.gnd.persistence.remote.RemoteDataStore;
import com.google.android.gnd.rx.annotations.Cold;
import io.reactivex.Maybe;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class TermsOfServiceRepository {

  private static final long LOAD_REMOTE_PROJECT_TERMS_OF_SERVICE_TIMEOUT_SECS = 30;

  private final RemoteDataStore remoteDataStore;
  private final LocalValueStore localValueStore;

  @Inject
  public TermsOfServiceRepository(
      RemoteDataStore remoteDataStore, LocalValueStore localValueStore) {
    this.remoteDataStore = remoteDataStore;
    this.localValueStore = localValueStore;
  }

  @Cold
  public Maybe<TermsOfService> getTermsOfService() {
    return remoteDataStore
        .loadTermsOfService()
        .timeout(LOAD_REMOTE_PROJECT_TERMS_OF_SERVICE_TIMEOUT_SECS, TimeUnit.SECONDS);
  }

  public boolean isTermsOfServiceAccepted() {
    return localValueStore.isTermsOfServiceAccepted();
  }

  public void setTermsOfServiceAccepted(boolean value) {
    localValueStore.setTermsOfServiceAccepted(value);
  }
}
