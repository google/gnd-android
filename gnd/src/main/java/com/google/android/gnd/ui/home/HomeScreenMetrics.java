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

package com.google.android.gnd.ui.home;

import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.gnd.R;
import com.google.android.gnd.ui.common.BottomSheetBehavior;

/** Abstracts access to dimensions and positions of elements on the main map / bottom sheet UI. */
public class HomeScreenMetrics {
  private final CoordinatorLayout parent;
  private final View bottomSheet;
  private final View addObservationButton;

  HomeScreenMetrics(CoordinatorLayout parent) {
    this.parent = parent;
    this.bottomSheet = parent.findViewById(R.id.feature_bottom_sheet);
    this.addObservationButton = parent.findViewById(R.id.add_observation_btn);
  }

  /** Returns the number of pixels of the bottom sheet visible above the bottom of the screen. */
  public float getBottomSheetVisibleHeight() {
    return Math.max(parent.getHeight() - bottomSheet.getTop(), 0);
  }

  /**
   * Returns the number of pixels between the top of the "Add Observation" button and the bottom of
   * the screen.
   */
  private float getAddObservationButtonDistanceFromBottom() {
    return Math.max(parent.getHeight() - addObservationButton.getTop(), 0);
  }

  /**
   * Returns a ratio indicating bottom sheet scroll progress from hidden to visible state.
   * Specifically, it returns 0 when the bottom sheet is fully hidden, 1 when its top just passes
   * the top of the "Add Observation" button, and a linearly interpolated ratio for all values in
   * between.
   */
  public float getBottomSheetVisibilityRatio() {
    return Math.min(
        getBottomSheetVisibleHeight() / getAddObservationButtonDistanceFromBottom(), 1.0f);
  }

  /**
   * Returns the "peek height" of the bottom sheet, the height of the sheet when it is initially
   * displayed and to which it snaps in "collapsed" state (between full expanded and fully hidden).
   */
  public int getBottomSheetPeekHeight() {
    return BottomSheetBehavior.from(bottomSheet).getPeekHeight();
  }
}