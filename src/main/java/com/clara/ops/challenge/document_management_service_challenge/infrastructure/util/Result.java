package com.clara.ops.challenge.document_management_service_challenge.infrastructure.util;

import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T, E extends Exception> permits Result.Success, Result.Failure {

  record Success<T, E extends Exception>(T value) implements Result<T, E> {
    @Override
    public T getValue() {
      return value;
    }

    @Override
    public E getError() {
      throw new UnsupportedOperationException("Success does not have an error.");
    }
  }

  record Failure<T, E extends Exception>(E error) implements Result<T, E> {
    @Override
    public T getValue() {
      throw new UnsupportedOperationException("Failure does not have a value.");
    }

    @Override
    public E getError() {
      return error;
    }
  }

  static <T, E extends Exception> Result<T, E> success(T value) {
    return new Success<>(value);
  }

  static <T, E extends Exception> Result<T, E> failure(E error) {
    return new Failure<>(error);
  }

  static <T> Result<T, Exception> of(Supplier<T> supplier) {
    try {
      return success(supplier.get());
    } catch (Exception e) {
      return failure(e);
    }
  }

  T getValue();

  E getError();

  default boolean isSuccess() {
    return this instanceof Success<T, E>;
  }

  default boolean isFailure() {
    return this instanceof Failure<T, E>;
  }

  default T getOrThrow() throws E {
    if (this instanceof Success<T, E> s) return s.value();
    throw ((Failure<T, E>) this).error();
  }

  default <R> R fold(Function<T, R> onSuccess, Function<E, R> onFailure) {
    if (this instanceof Success<T, E> s) return onSuccess.apply(s.value());
    return onFailure.apply(((Failure<T, E>) this).error());
  }

  @SuppressWarnings("unchecked")
  default <R> Result<R, E> map(Function<T, R> mapper) {
    if (this instanceof Success<T, E> s) {
      try {
        return Result.success(mapper.apply(s.value()));
      } catch (Exception e) {
        return Result.failure((E) e);
      }
    }
    return (Result<R, E>) this;
  }

  @SuppressWarnings("unchecked")
  default <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
    if (this instanceof Success<T, E> s) {
      try {
        return mapper.apply(s.value());
      } catch (Exception e) {
        return Result.failure((E) e);
      }
    }
    return (Result<R, E>) this;
  }
}
