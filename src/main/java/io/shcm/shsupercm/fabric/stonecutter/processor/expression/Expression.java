package io.shcm.shsupercm.fabric.stonecutter.processor.expression;

import java.util.function.BiFunction;

public interface Expression extends BiFunction<Boolean, String, ExpressionResult> {
}