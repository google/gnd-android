/*
 * Copyright 2020 Google LLC
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

package com.google.android.gnd.ui.common;

import com.google.android.gnd.model.form.Field;
import com.google.android.gnd.model.observation.Response;
import java8.util.Optional;

public final class Validator {

  public static boolean isValid(Field field, String textResponse) {
    if (!field.isRequired()) {
      return true;
    }
    return textResponse != null && !textResponse.isEmpty();
  }

  public static boolean isValid(Field field, Optional<Response> response) {
    if (!field.isRequired()) {
      return true;
    }
    return !response.isEmpty();
  }
}
