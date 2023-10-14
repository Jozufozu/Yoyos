package com.jozufozu.yoyos.core.control;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TargetProviderStack {
    private final Stack<TargetProvider> targetProviderStack = new ObjectArrayList<>();

    public void push(TargetProvider targetProvider) {
        targetProviderStack.push(targetProvider);
    }

    @Nullable
    public TargetProvider get() {
        // Step down the stack until we either run out or find a valid provider.
        while (true) {
            if (targetProviderStack.isEmpty()) {
                return null;
            }

            var provider = targetProviderStack.top();

            if (!provider.isExpired()) {
                return provider;
            }

            targetProviderStack.pop();
        }
    }
}
