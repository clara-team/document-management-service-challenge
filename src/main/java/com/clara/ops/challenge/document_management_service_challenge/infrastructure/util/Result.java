package com.clara.ops.challenge.document_management_service_challenge.infrastructure.util;

import java.util.function.Function;

public sealed interface Result<T, E extends Exception> permits Result.Success, Result.Failure {

  record Success<T, E extends Exception>(T value) implements Result<T, E> {}

  record Failure<T, E extends Exception>(E error) implements Result<T, E> {}

  static <T, E extends Exception> Result<T, E> success(T value) {
    return new Success<>(value);
  }

  static <T, E extends Exception> Result<T, E> failure(E error) {
    return new Failure<>(error);
  }

  default boolean isSuccess() {
    return this instanceof Success;
  }

  default boolean isFailure() {
    return this instanceof Failure;
  }

  default T getOrThrow() throws E {
    if (this instanceof Success<T, E> s) return s.value();
    throw ((Failure<T, E>) this).error();
  }

  default <R> R fold(Function<T, R> onSuccess, Function<E, R> onFailure) {
    if (this instanceof Success<T, E> s) return onSuccess.apply(s.value());
    return onFailure.apply(((Failure<T, E>) this).error());
  }
}
