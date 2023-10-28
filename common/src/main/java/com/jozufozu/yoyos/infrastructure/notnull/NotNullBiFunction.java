package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface NotNullBiFunction<@NotNullType T, @NotNullType U, @NotNullType R> extends BiFunction<T, U, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    @Override
    R apply(T t, U u);

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> NotNullBiFunction<T, U, V> andThen(NotNullFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
