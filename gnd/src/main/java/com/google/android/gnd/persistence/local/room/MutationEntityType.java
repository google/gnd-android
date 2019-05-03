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

package com.google.android.gnd.persistence.local.room;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;
import com.google.android.gnd.persistence.shared.Mutation;

public enum MutationEntityType implements IntEnum {
  UNKNOWN(0),
  CREATE(1),
  UPDATE(2),
  DELETE(3),
  RELOAD(4);

  private final int intValue;

  MutationEntityType(int intValue) {
    this.intValue = intValue;
  }

  static MutationEntityType fromMutationType(Mutation.Type type) {
    switch (type) {
      case CREATE:
        return CREATE;
      case UPDATE:
        return UPDATE;
      case DELETE:
        return DELETE;
      case RELOAD:
        return RELOAD;
      default:
        return UNKNOWN;
    }
  }

  public int intValue() {
    return intValue;
  }

  @TypeConverter
  public static int toInt(@Nullable MutationEntityType value) {
    return IntEnum.toInt(value, UNKNOWN);
  }

  @NonNull
  @TypeConverter
  public static MutationEntityType fromInt(int intValue) {
    return IntEnum.fromInt(values(), intValue, UNKNOWN);
  }
}
