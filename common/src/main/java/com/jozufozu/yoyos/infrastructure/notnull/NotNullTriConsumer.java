package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.Objects;

import org.apache.logging.log4j.util.TriConsumer;

public interface NotNullTriConsumer<@NotNullType T, @NotNullType U, @NotNullType V> extends TriConsumer<T, U, V> {
    static <T, U, V> NotNullTriConsumer<T, U, V> noop() {
        return (t, u, v) -> {};
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Override
    void accept(T t, U u, V v);

    /**
     * Returns a composed {@code NotNullBiConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code NotNullBiConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default NotNullTriConsumer<T, U, V> andThen(NotNullTriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> { accept(t, u, v); after.accept(t, u, v); };
    }

    default NotNullTriConsumer<T, U, V> butFirst(NotNullTriConsumer<? super T, ? super U, ? super V> before) {
        Objects.requireNonNull(before);
        return (T t, U u, V v) -> { before.accept(t, u, v); accept(t, u, v); };
    }

    default NotNullBiConsumer<U, V> applyFirst(NotNullSupplier<T> first) {
        Objects.requireNonNull(first);
        return (u, v) -> accept(first.get(), u, v);
    }

    default NotNullBiConsumer<U, V> applyFirst(T first) {
        Objects.requireNonNull(first);
        return (u, v) -> accept(first, u, v);
    }

    default NotNullBiConsumer<T, V> applySecond(NotNullSupplier<U> second) {
        Objects.requireNonNull(second);
        return (t, v) -> accept(t, second.get(), v);
    }
}
