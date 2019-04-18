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
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import org.json.JSONObject;

/** Representation of a {@link com.google.android.gnd.vo.Record} in local db. */
@AutoValue
@Entity(
    tableName = "record",
    indices = {@Index("id")})
public abstract class RecordEntity {
  @CopyAnnotations
  @PrimaryKey
  @ColumnInfo(name = "id")
  @NonNull
  public abstract String getId();

  /** Returns the id of the form to which this record's responses apply. */
  @CopyAnnotations
  @ColumnInfo(name = "form_id")
  @NonNull
  public abstract String getFormId();

  @CopyAnnotations
  @ColumnInfo(name = "state")
  @NonNull
  public abstract EntityState getState();

  /**
   * Returns a JSON object containing user responses keyed by their respective elementId in the form
   * identified by formId. Returns an empty JSON object if no responses have been provided.
   */
  @CopyAnnotations
  @ColumnInfo(name = "responses")
  @NonNull
  public abstract JSONObject getResponses();

  // Auto-generated boilerplate:

  public static RecordEntity create(
      String id, EntityState state, String formId, JSONObject responses) {
    return builder().setId(id).setState(state).setResponses(responses).setFormId(formId).build();
  }

  public static Builder builder() {
    return new AutoValue_RecordEntity.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setState(EntityState newState);

    public abstract Builder setFormId(String newFormId);

    public abstract Builder setResponses(JSONObject newResponses);

    public abstract RecordEntity build();
  }
}
