package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

public interface NotNullBiConsumer<T, U> extends BiConsumer<T, U> {
    static <T, U> NotNullBiConsumer<T, U> noop() {
        return (t, u) -> {};
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Override
    void accept(@NotNull T t, @NotNull U u);

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
}
