package io.shcm.shsupercm.fabric.stonecutter.version;

import io.shcm.shsupercm.fabric.stonecutter.processor.expression.ExpressionResult;

import java.util.function.Predicate;

public interface VersionChecker {
    Object parseVersion(String versionString) throws Exception;

    Predicate<Object> parseChecker(String predicateString) throws Exception;

    default ExpressionResult check(Object version, String predicateString) {
        try {
            return ExpressionResult.of(parseChecker(predicateString).test(version));
        } catch (Exception e) {
            return ExpressionResult.INVALID;
        }
    }
}
