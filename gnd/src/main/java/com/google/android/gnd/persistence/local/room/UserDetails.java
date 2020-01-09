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

package com.google.android.gnd.persistence.local.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import com.google.android.gnd.model.User;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;

/** Component representing cached user details in local db entities. */
@AutoValue
public abstract class UserDetails {

  @CopyAnnotations
  @NonNull
  @ColumnInfo(name = "id")
  public abstract String getId();

  @CopyAnnotations
  @NonNull
  @ColumnInfo(name = "email")
  public abstract String getEmail();

  @CopyAnnotations
  @NonNull
  @ColumnInfo(name = "display_name")
  public abstract String getDisplayName();

  public static UserDetails fromUser(User user) {
    return UserDetails.builder()
        .setId(user.getId())
        .setEmail(user.getEmail())
        .setDisplayName(user.getDisplayName())
        .build();
  }

  public static User toUser(UserDetails d) {
    return User.builder()
        .setId(d.getId())
        .setEmail(d.getEmail())
        .setDisplayName(d.getDisplayName())
        .build();
  }

  public static UserDetails create(String id, String email, String displayName) {
    return builder().setId(id).setEmail(email).setDisplayName(displayName).build();
  }

  // Generated by AutoValue plugin:

  public static Builder builder() {
    return new AutoValue_UserDetails.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setEmail(String email);

    public abstract Builder setDisplayName(String displayName);

    public abstract UserDetails build();
  }
}
