package io.shcm.shsupercm.fabric.stonecutter.processor.expression;

import io.shcm.shsupercm.fabric.stonecutter.version.VersionChecker;

public class McVersionExpression implements Expression {
    private final Object current;
    private final VersionChecker checker;

    public McVersionExpression(Object current, VersionChecker checker) {
        this.current = current;
        this.checker = checker;
    }
    @Override
    public ExpressionResult apply(Boolean prev, String expression) {
        return checker.check(current, expression);
    }
}