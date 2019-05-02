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

package com.google.android.gnd.persistence.local.change;

import com.google.android.gnd.vo.Record.Response;
import com.google.auto.value.AutoValue;
import java8.util.Optional;

/** Represents a change in a form response. */
@AutoValue
public abstract class ResponseChange implements AttributeChange {
  /** Returns the unique id of the element being modified. */
  public abstract String getElementId();

  /**
   * Returns the response to be set after this change is applied. If empty, the response indicates
   * the current response should be cleared.
   */
  public abstract Optional<Response> getNewResponse();

  public static Builder builder() {
    return new AutoValue_ResponseChange.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setElementId(String newElementId);

    public abstract Builder setNewResponse(Optional<Response> newNewResponse);

    public abstract ResponseChange build();
  }
}
