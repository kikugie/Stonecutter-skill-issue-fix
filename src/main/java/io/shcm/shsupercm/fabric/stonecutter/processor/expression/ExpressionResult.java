package io.shcm.shsupercm.fabric.stonecutter.processor.expression;

import org.jetbrains.annotations.Nullable;

public enum ExpressionResult {
    TRUE(true),
    FALSE(false),
    INVALID(null);

    public final Boolean value;

    ExpressionResult(@Nullable Boolean value) {
        this.value = value;
    }

    public static ExpressionResult of(@Nullable Boolean value) {
        if (value == null) return INVALID;
        return value ? TRUE : FALSE;
    }
}