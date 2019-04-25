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

package com.google.android.gnd.rx;

import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import javax.annotation.Nullable;

public class Result<T> {
  public enum State {
    SUCCESS,
    ERROR
  }

  private final State state;

  @Nullable private final T value;
  @Nullable private final Throwable error;

  private Result(State state, @Nullable T value, @Nullable Throwable error) {
    this.state = state;
    this.value = value;
    this.error = error;
  }

  public static <T> Result<T> success(@NonNull T result) {
    return new Result<>(State.SUCCESS, result, null);
  }

  public static <T> Result<T> error(@NonNull Throwable t) {
    return new Result<>(State.ERROR, null, t);
  }

  @Nullable
  public State getState() {
    return state;
  }

  @Nullable
  public T get() {
    return value;
  }

  @Nullable
  public Throwable getError() {
    return error;
  }

  public static <T, R> Function<T, Single<Result<R>>> wrapErrors(
      @NonNull Function<T, Single<R>> fn) {
    return (T value) -> fn.apply(value).map(Result::success).onErrorReturn(Result::error);
  }

  public static <T> Consumer<? super Result<T>> unwrapErrors(
      Consumer<T> onSuccess, Consumer<Throwable> onError) {
    return result -> {
      switch (result.getState()) {
        case SUCCESS:
          onSuccess.accept(result.get());
          break;
        case ERROR:
          onError.accept(result.getError());
          break;
      }
    };
  }
}
