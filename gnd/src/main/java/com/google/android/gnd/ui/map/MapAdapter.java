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

package com.google.android.gnd.ui.map;

import android.annotation.SuppressLint;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gnd.model.feature.Point;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Observable;

/**
 * Interface defining map interactions and events. This a separate class from {@link MapProvider} so
 * that it can be returned asynchronously by {@link MapProvider#getMapAdapter()} if necessary.
 */
public interface MapAdapter {

  /** Returns a stream that emits map pins clicked by the user. */
  Observable<MapPin> getMapPinClicks();

  /**
   * Returns a stream that emits the new viewport center each time the map is dragged by the user.
   */
  Observable<Point> getDragInteractions();

  /** Returns a stream that emits the viewport center on each camera movement. */
  Observable<Point> getCameraMoves();

  /** Enables map gestures like pan and zoom. */
  void enable();

  /** Disables all map gestures like pan and zoom. */
  void disable();

  /**
   * Repositions the viewport centered around the specified point without changing the current zoom
   * level.
   */
  void moveCamera(Point point);

  /**
   * Repositions the viewport centered around the specified point while also updating the current
   * zoom level.
   */
  void moveCamera(Point point, float zoomLevel);

  /** Returns the current center of the viewport. */
  Point getCameraTarget();

  /** Returns the current map zoom level. */
  float getCurrentZoomLevel();

  /** Displays user location indicator on the map. */
  @SuppressLint("MissingPermission")
  void enableCurrentLocationIndicator();

  /** Update map pins shown on map. */
  void setMapPins(ImmutableSet<MapPin> pins);

  /** Get current map type. */
  int getMapType();

  /** Update map type. */
  void setMapType(int mapType);

  /** Returns the bounds of the currently visibly viewport. */
  LatLngBounds getViewport();

  /** Renders a tile overlay on the map. */
  void renderTileOverlay();
}
