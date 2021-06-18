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

package com.google.android.gnd.system;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import com.google.android.gnd.BuildConfig;
import com.google.android.gnd.rx.Nil;
import com.google.android.gnd.rx.annotations.Cold;
import com.google.android.gnd.rx.annotations.Hot;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import java.io.File;
import javax.inject.Inject;
import javax.inject.Singleton;
import timber.log.Timber;

/** Manages permissions needed for using camera and related flows to/from Activity. */
@Singleton
public class CameraManager {

  /** Used to identify requests coming from this application. */
  static final int CAPTURE_PHOTO_REQUEST_CODE = CameraManager.class.hashCode() & 0xffff;

  private final Context context;
  private final PermissionsManager permissionsManager;
  private final ActivityStreams activityStreams;

  @Inject
  public CameraManager(
      @ApplicationContext Context context,
      PermissionsManager permissionsManager,
      ActivityStreams activityStreams) {
    this.context = context;
    this.permissionsManager = permissionsManager;
    this.activityStreams = activityStreams;
  }

  /** Launches the system's photo capture flow, first obtaining permissions if necessary. */
  @Cold
  public Maybe<Nil> capturePhoto(File destFile) {
    return permissionsManager
        .obtainPermission(permission.WRITE_EXTERNAL_STORAGE)
        .andThen(permissionsManager.obtainPermission(permission.CAMERA))
        .andThen(sendCapturePhotoIntent(destFile))
        .andThen(capturePhotoResult());
  }

  /** Enqueue an intent for capturing a photo. */
  @Cold
  private Completable sendCapturePhotoIntent(File photoFile) {
    return Completable.fromAction(
        () ->
            activityStreams.withActivity(
                activity -> {
                  Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                  Uri photoUri =
                      FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, photoFile);
                  cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                  activity.startActivityForResult(cameraIntent, CAPTURE_PHOTO_REQUEST_CODE);
                  Timber.v("Capture photo intent sent. Image path: %s", photoFile);
                }));
  }

  /** Emits the result of the photo capture request. */
  @Hot(terminates = true)
  Maybe<Nil> capturePhotoResult() {
    return activityStreams
        .getNextActivityResult(CAPTURE_PHOTO_REQUEST_CODE)
        .flatMapMaybe(this::onCapturePhotoResult)
        .singleElement();
  }

  /** Returns success if the result is ok. */
  @Cold
  private Maybe<Nil> onCapturePhotoResult(ActivityResult result) {
    Timber.v("Photo result returned");
    return Maybe.create(
        emitter -> {
          if (result.isOk()) {
            emitter.onSuccess(Nil.NIL);
          } else {
            emitter.onComplete();
          }
        });
  }
}
