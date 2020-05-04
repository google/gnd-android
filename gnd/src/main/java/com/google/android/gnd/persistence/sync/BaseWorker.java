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

package com.google.android.gnd.persistence.sync;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gnd.persistence.remote.TransferProgress;
import com.google.android.gnd.system.NotificationManager;
import io.reactivex.Completable;
import io.reactivex.Flowable;

public abstract class BaseWorker extends Worker {

  private final NotificationManager notificationManager;
  private final int notificationId;

  BaseWorker(
      @NonNull Context context,
      @NonNull WorkerParameters workerParams,
      NotificationManager notificationManager,
      int notificationId) {
    super(context, workerParams);
    this.notificationManager = notificationManager;
    this.notificationId = notificationId;
  }

  /** Content text displayed in the notification. */
  public abstract String getNotificationTitle();

  <T> Flowable<T> notifyTransferState(Flowable<T> upstream) {
    return upstream
        .doOnSubscribe(__ -> sendNotification(TransferProgress.starting()))
        .doOnError(__ -> sendNotification(TransferProgress.failed()))
        .doOnComplete(() -> sendNotification(TransferProgress.completed()));
  }

  Completable notifyTransferState(Completable completable) {
    return completable
        .doOnSubscribe(__ -> sendNotification(TransferProgress.starting()))
        .doOnError(__ -> sendNotification(TransferProgress.failed()))
        .doOnComplete(() -> sendNotification(TransferProgress.completed()));
  }

  private Notification createNotification(TransferProgress transferProgress) {
    return notificationManager.createSyncNotification(
        transferProgress.getState(),
        getNotificationTitle(),
        transferProgress.getByteCount(),
        transferProgress.getBytesTransferred());
  }

  /**
   * Specifies that this is a long-running request and should be kept alive by the OS. Also, runs a
   * foreground service under the hood to execute the request showing a notification.
   */
  void sendNotification(TransferProgress progress) {
    setForegroundAsync(new ForegroundInfo(notificationId, createNotification(progress)));
  }
}
