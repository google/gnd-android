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

package com.google.android.gnd.repository.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import io.reactivex.Completable;
import io.reactivex.Single;

/** Data access object for database operations related to {@link FeatureEntity}. */
@Dao
public interface FeatureDao {
  @Insert
  Single<Long> insert(FeatureEntity feature);

  @Update
  Completable update(FeatureEntity feature);

  @Delete
  Completable delete(FeatureEntity feature);
}
