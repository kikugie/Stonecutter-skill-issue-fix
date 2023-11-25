package io.shcm.shsupercm.fabric.stonecutter.processor;

import io.shcm.shsupercm.fabric.stonecutter.cutter.StonecutterSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.shcm.shsupercm.fabric.stonecutter.processor.ExpressionProcessor.ExpressionResult.*;

public class ExpressionProcessor {
    /* Testing */
    public static final Expression BASIC = ($, e) -> e.equals("true") ? TRUE : e.equals("false") ? FALSE : INVALID;

    /* Actual */
    public static final Expression ELSE = (p, e) -> e.equals("else") ? ExpressionResult.of(!p) : INVALID;

    public static boolean testStatic(boolean previous, String expression) throws StonecutterSyntaxException {
        ExpressionResult ifElse = ELSE.apply(previous, expression);
        if (ifElse != INVALID) return ifElse.value;

        ExpressionResult result = BASIC.apply(previous, expression);
        if (result == INVALID)
            throw new StonecutterSyntaxException("Invalid expression: " + expression);
        return result.value;
    }


    public interface Expression extends BiFunction<Boolean, String, ExpressionResult> {
    }

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
}