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

package com.google.android.gnd;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.work.Configuration;
import androidx.work.WorkManager;
import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.google.android.gnd.inject.GndWorkerFactory;
import com.google.android.gnd.rx.RxDebug;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import io.reactivex.plugins.RxJavaPlugins;
import javax.inject.Inject;
import timber.log.Timber;

// TODO: When implementing background data sync service, we'll need to inject a Service here; we
// should then extend DaggerApplication instead. If MultiDex is still needed, we can install it
// without extending MultiDexApplication.
public class GndApplication extends DaggerApplication {

  @Inject GndWorkerFactory workerFactory;

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  @Override
  public void onCreate() {
    if (BuildConfig.DEBUG) {
      Timber.d("DEBUG build config active; enabling debug tooling");

      // Debug bridge for Android applications. Enables network and database debugging for the app
      // accessible under chrome://inspect in Chrome desktop browser. Must be done before calling
      // setStrictMode().
      Stetho.initializeWithDefaults(this);

      // Log failures when trying to do work in the UI thread.
      setStrictMode();
    }

    super.onCreate();

    // Enable RxJava assembly stack collection for more useful stack traces.
    RxJava2Debug.enableRxJava2AssemblyTracking(new String[] {getClass().getPackage().getName()});

    // Prevent RxJava from force-quitting on unhandled errors.
    RxJavaPlugins.setErrorHandler(RxDebug::logEnhancedStackTrace);

    // Set custom worker factory that allow Workers to use Dagger injection.
    // TODO(github.com/google/dagger/issues/1183): Remove once Workers support injection.
    WorkManager.initialize(
        this, new Configuration.Builder().setWorkerFactory(workerFactory).build());

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    } else {
      Timber.plant(new CrashReportingTree());
    }
  }

  @Override
  protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
    // Root of dependency injection.
    return DaggerGndApplicationComponent.factory().create(this);
  }

  private void setStrictMode() {
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

    StrictMode.setVmPolicy(
        new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().build());
  }

  private static class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable throwable) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
        return;
      }

      Crashlytics.log(priority, tag, message);

      if (throwable != null && priority == Log.ERROR) {
        Crashlytics.logException(throwable);
      }
    }
  }
}
