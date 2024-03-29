package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.Objects;
import java.util.function.Consumer;

public interface NotNullConsumer<@NotNullType T> extends Consumer<T> {

    static <T> NotNullConsumer<T> noop() {
        return $ -> {};
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Override
    void accept(T t);

    /**
     * Returns a composed {@code NotNullConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code NotNullConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default NotNullConsumer<T> andThen(NotNullConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }

    default NotNullConsumer<T> butFirst(NotNullConsumer<T> before) {
        Objects.requireNonNull(before);
        return (T t) -> { before.accept(t); accept(t); };
    }
}
