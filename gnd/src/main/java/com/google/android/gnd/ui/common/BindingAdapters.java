/*
 * Copyright 2018 Google LLC
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

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import com.google.android.gms.common.SignInButton;
import com.google.android.gnd.R;
import com.google.android.gnd.ui.field.MultipleChoiceFieldLayout;
import com.squareup.picasso.Picasso;

/**
 * Container for adapter methods defining custom data binding behavior. This class cannot be made
 * injectable, since binding adapters must be static.
 */
public class BindingAdapters {

  @BindingAdapter("onClick")
  public static void bindGoogleSignOnButtonClick(
      SignInButton button, View.OnClickListener onClickCallback) {
    button.setOnClickListener(onClickCallback);
  }

  @BindingAdapter("onShowDialog")
  public static void setOnShowDialogListener(MultipleChoiceFieldLayout view, Runnable listener) {
    view.setOnShowDialogListener(listener);
  }

  @BindingAdapter("imageUri")
  public static void bindUri(ImageView view, Uri uri) {
    if (uri != null) {
      Picasso.get().load(uri).placeholder(R.drawable.ic_photo_grey_600_24dp).into(view);
    }
  }
}
