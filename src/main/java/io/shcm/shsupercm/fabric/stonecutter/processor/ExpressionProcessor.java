package io.shcm.shsupercm.fabric.stonecutter.processor;

import io.shcm.shsupercm.fabric.stonecutter.cutter.StonecutterSyntaxException;
import io.shcm.shsupercm.fabric.stonecutter.processor.expression.Expression;
import io.shcm.shsupercm.fabric.stonecutter.processor.expression.ExpressionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.shcm.shsupercm.fabric.stonecutter.processor.expression.ExpressionResult.*;

public class ExpressionProcessor {
    /* Testing */
    public static final Expression BASIC = ($, e) -> e.equals("true") ? TRUE : e.equals("false") ? FALSE : INVALID;
    public static final ExpressionProcessor TEST = new ExpressionProcessor(Collections.emptyList(), true);
    /* --- */
    public static final Expression ELSE = (p, e) -> e.startsWith("else") ? ExpressionResult.of(!p) : INVALID;
    private final List<Expression> checkers = new ArrayList<>();

    public ExpressionProcessor(Collection<Expression> checkers, boolean debug) {
        if (debug) this.checkers.add(BASIC);
        this.checkers.addAll(checkers);
    }

    public boolean test(boolean previous, String expression) throws StonecutterSyntaxException {
        if (previous) return false;
        ExpressionResult ifElse = ELSE.apply(previous, expression);
        if (ifElse != INVALID) expression = expression.substring(4).trim();
        if (expression.isEmpty()) return ifElse.value;

        for (Expression checker : checkers) {
            ExpressionResult result = checker.apply(previous, expression);
            if (result != INVALID) return result.value;
        }
        throw new StonecutterSyntaxException("Invalid expression: " + expression);
    }

    public void addChecker(Expression expression) {
        checkers.add(expression);
    }

    public void addCheckers(Collection<Expression> expressions) {
        checkers.addAll(expressions);
    }
}