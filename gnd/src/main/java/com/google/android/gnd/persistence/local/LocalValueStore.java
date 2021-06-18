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

import static java8.util.J8Arrays.stream;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import com.google.android.gnd.model.feature.Point;
import com.google.android.gnd.ui.map.CameraPosition;
import com.google.android.gnd.ui.settings.Keys;
import java8.util.Optional;
import java8.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import timber.log.Timber;

/**
 * Simple value store persisted locally on device. Unlike {@link LocalDataStore}, this class
 * provides a concrete implementation using the Android SDK, and therefore does not require a
 * database-specific implementation.
 */
@Singleton
public class LocalValueStore {

  public static final String ACTIVE_PROJECT_ID_KEY = "activeProjectId";
  public static final String MAP_TYPE = "map_type";
  public static final String LAST_VIEWPORT_PREFIX = "last_viewport_";

  private final SharedPreferences preferences;

  @Inject
  public LocalValueStore(SharedPreferences preferences) {
    this.preferences = preferences;
  }

  /** Returns the id of the last project successfully activated by the user, or null if not set. */
  @Nullable
  public String getLastActiveProjectId() {
    return preferences.getString(ACTIVE_PROJECT_ID_KEY, null);
  }

  /** Set the id of the last project successfully activated by the user. */
  public void setLastActiveProjectId(@NonNull String id) {
    preferences.edit().putString(ACTIVE_PROJECT_ID_KEY, id).apply();
  }

  /** Removes all values stored in the local store. */
  public void clear() {
    preferences.edit().clear().apply();
  }

  public boolean shouldUploadMediaOverUnmeteredConnectionOnly() {
    return preferences.getBoolean(Keys.UPLOAD_MEDIA, false);
  }

  public boolean shouldDownloadOfflineAreasOverUnmeteredConnectionOnly() {
    return preferences.getBoolean(Keys.OFFLINE_AREAS, false);
  }

  public void saveMapType(int type) {
    preferences.edit().putInt(MAP_TYPE, type).apply();
  }

  public int getSavedMapType(int defaultType) {
    return preferences.getInt(MAP_TYPE, defaultType);
  }

  public void setLastCameraPosition(String projectId, CameraPosition cameraPosition) {
    Double[] values = {
      cameraPosition.getTarget().getLatitude(),
      cameraPosition.getTarget().getLongitude(),
      (double) cameraPosition.getZoomLevel()
    };
    String value = stream(values).map(d -> String.valueOf(d)).collect(Collectors.joining(","));
    preferences.edit().putString(LAST_VIEWPORT_PREFIX + projectId, value).apply();
  }

  public Optional<CameraPosition> getLastCameraPosition(String projectId) {
    try {
      String[] values = preferences.getString(LAST_VIEWPORT_PREFIX + projectId, "").split(",");
      return Optional.of(
          new CameraPosition(
              Point.newBuilder()
                  .setLatitude(Double.valueOf(values[0]))
                  .setLongitude(Double.valueOf(values[1]))
                  .build(),
              Float.valueOf(values[2])));
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
      Timber.e(e, "Invalid camera pos in prefs");
      return Optional.empty();
    }
  }
}
