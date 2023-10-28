package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.Objects;
import java.util.function.BiConsumer;

public interface NotNullBiConsumer<@NotNullType T, @NotNullType U> extends BiConsumer<T, U> {
    static <T, U> NotNullBiConsumer<T, U> noop() {
        return (t, u) -> {};
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Override
    void accept(T t, U u);

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
    default NotNullBiConsumer<T, U> andThen(NotNullBiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> { accept(t, u); after.accept(t, u); };
    }

    default NotNullConsumer<U> applyFirst(NotNullSupplier<T> first) {
        Objects.requireNonNull(first);
        return u -> accept(first.get(), u);
    }

    default NotNullConsumer<U> applyFirst(T first) {
        Objects.requireNonNull(first);
        return u -> accept(first, u);
    }

    default NotNullConsumer<T> applySecond(NotNullSupplier<U> second) {
        Objects.requireNonNull(second);
        return t -> accept(t, second.get());
    }

    default NotNullBiConsumer<U, T> flipArgs() {
        return (u, t) -> accept(t, u);
    }
}
