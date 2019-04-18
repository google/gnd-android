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

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Main entry point to local database API, exposing data access objects (DAOs) for interacting with
 * various entities persisted in tables in db.
 *
 * <p>A separate data model is used to represent data stored locally to prevent leaking db-level
 * design details into main API * and to allow us to guarantee backwards compatibility.
 */
// TODO: Make DAOs injectable via Dagger.
// TODO: Make all Room impls package private.
@Database(
    entities = {
      FeatureEntity.class,
      FeatureEditEntity.class,
      RecordEntity.class,
      RecordEditEntity.class
    },
    version = 1,
    exportSchema = false)
@TypeConverters({Edit.Type.class, EntityState.class, JSONObjectTypeConverter.class})
public abstract class GndRoomDatabase extends RoomDatabase {

  public abstract FeatureDao featureDao();

  public abstract FeatureEditDao featureEditDao();

  public abstract RecordDao recordDao();

  public abstract RecordEditDao recordEditDao();
}
