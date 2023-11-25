package io.shcm.shsupercm.fabric.stonecutter.processor;

import io.shcm.shsupercm.fabric.stonecutter.cutter.StonecutterSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Stack;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentProcessor {
    public static final String START = "/*?";
    public static final String END = "*/";

    private final Stack<LinkedList<Token>> conditions = new Stack<>();
    private final Reader input;
    private final StringBuilder output;
    private final ExpressionProcessor processor;

    public CommentProcessor(Reader input, StringBuilder output, ExpressionProcessor processor) {
        this.input = input;
        this.output = output;
        this.processor = processor;
    }

    public static StringBuilder process(Reader input, ExpressionProcessor processor) throws Exception {
        StringBuilder builder = new StringBuilder();
        CommentProcessor commentProcessor = new CommentProcessor(input, builder, processor);
        commentProcessor.process();
        return builder;
    }

    public void process() throws StonecutterSyntaxException, IOException {
        while (true) {
            if (!next()) break;
        }
    }

    private boolean next() throws IOException, StonecutterSyntaxException {
        String previous = read(START);
        if (previous == null) return false;

        String expression = readExpression();
        addExpression(expression, previous);
        return true;
    }

    private void addExpression(String expression, String prev) throws StonecutterSyntaxException, IOException {
        if (expression.contains(START))
            throw new StonecutterSyntaxException("Expression wasn't correctly closed: " + expression);

        ExpressionType type = ExpressionType.of(expression);
        switch (type) {
            case SINGLE -> single(expression);
            case OPENER -> opener(expression);
            case EXTENSION -> extension(expression, prev);
            case CLOSER -> closer(expression, prev);
        }
    }

    private void single(String expression) throws StonecutterSyntaxException, IOException {
        boolean enabled = processExpression(ExpressionType.SINGLE.format(expression));
        String code = readRegex("\\S\\s*(\\r\\n|\\r|\\n)", true);
        if (code == null)
            throw new StonecutterSyntaxException("No end of line found. How peculiar");
        String trimmedCode = code.replaceAll("(\\r\\n|\\r|\\n)", "");

        processCode(enabled, trimmedCode);
    }

    private void opener(String expression) throws StonecutterSyntaxException {
        Token token = new Token(ExpressionType.OPENER, processExpression(ExpressionType.OPENER.format(expression)));
        LinkedList<Token> list = new LinkedList<>();
        list.add(token);
        conditions.push(list);
    }

    private void extension(String expression, String prev) throws StonecutterSyntaxException {
        Token lastToken = conditions.peek().getLast();
        if (lastToken == null || (lastToken.type != ExpressionType.OPENER && lastToken.type != ExpressionType.EXTENSION))
            throw new StonecutterSyntaxException("Extension token without context: " + expression);
        
        boolean enabled = processExpression(lastToken.enabled, ExpressionType.EXTENSION.format(expression));
        processCode(lastToken.enabled, prev);
        Token token = new Token(ExpressionType.EXTENSION, enabled);
        conditions.peek().add(token);
    }

    private void closer(String expression, String prev) throws StonecutterSyntaxException {
        Token lastToken = conditions.peek().getLast();
        if (lastToken == null || (lastToken.type != ExpressionType.OPENER && lastToken.type != ExpressionType.EXTENSION))
            throw new StonecutterSyntaxException("Closer token without context: " + expression);
        processCode(lastToken.enabled, prev);
        conditions.pop();
    }

    private void processCode(boolean enabled, String prev) {
        boolean commented = isCommented(prev);
        if (enabled) {
            if (commented) removeCommentFor(prev);
        } else {
            if (!commented) addCommentFor(prev);
        }
    }

    private boolean processExpression(String expression) throws StonecutterSyntaxException {
        return processExpression(false, expression);
    }

    private boolean processExpression(boolean previous, String expression) throws StonecutterSyntaxException {
        return processor.test(previous, expression);
    }

    private String readExpression() throws StonecutterSyntaxException, IOException {
        String expression = read(END);
        if (expression == null)
            throw new StonecutterSyntaxException("Expected */ to close stonecutter expression");
        // Original stonecutter expressions must end with ?. In this fork it's not necessary, so this is for compat
        if (expression.endsWith("?")) expression = expression.substring(0, expression.length() - 1);
        return expression.trim();
    }


    private @Nullable String read(String match) throws IOException {
        return read(match, false);
    }

    private @Nullable String read(String match, boolean include) throws IOException {
        StringBuilder buffer = new StringBuilder();

        int current;
        while ((current = input.read()) != -1) {
            String ch = Character.toString(current);
            buffer.append(ch);
            output.append(ch);

            if (buffer.toString().endsWith(match))
                return include? buffer.toString() : buffer.substring(0, buffer.length() - match.length());
        }
        return null;
    }

    private @Nullable String readRegex(String regex, boolean include) throws IOException {
        StringBuilder buffer = new StringBuilder();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher("");
        input.mark(1);

        int current;
        while ((current = input.read()) != -1) {
            String ch = Character.toString(current);
            buffer.append(ch);
            output.append(ch);

            matcher.reset(buffer);
            if (matcher.find())
                if (!include) {
                    buffer.delete(matcher.start(), buffer.length());
                    return buffer.toString();
                } else {
                    if (matcher.end() != buffer.length()) {
                        buffer.deleteCharAt(buffer.length() - 1);
                        output.deleteCharAt(output.length() - 1);
                        input.reset();
                        return buffer.toString();
                    }
                }
            input.mark(1);
        }
        return buffer.isEmpty() ? null : buffer.toString();
    }

    private boolean isCommented(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("/*") && trimmed.endsWith("*/");
    }

    private void addCommentFor(String value) {
        replaceLast(value, "/*" + value + "*/");
    }

    private void removeCommentFor(String value) {
        String uncommented = replaceLast(value.replaceFirst("/\\*", ""), "*/", "");
        replaceLast(value, uncommented);
    }

    private void replaceLast(String target, String replacement) {
        int index = output.lastIndexOf(target);
        if (index != -1) {
            output.replace(index, index + target.length(), replacement);
        }
    }

    private String replaceLast(String text, String target, String replacement) {
        int pos = text.lastIndexOf(target);
        if (pos > -1) {
            return text.substring(0, pos) + replacement + text.substring(pos + target.length());
        } else {
            return text;
        }
    }

    public enum ExpressionType {
        SINGLE(String::trim),
        OPENER(str -> str.substring(0, str.length() - 2).trim()),
        EXTENSION(str -> str.substring(1, str.length() - 2).trim()),
        CLOSER(str -> str.substring(1).trim());

        private final UnaryOperator<String> formatter;

        ExpressionType(UnaryOperator<String> formatter) {
            this.formatter = formatter;
        }

        public String format(String expression) {
            return formatter.apply(expression);
        }

        public static ExpressionType of(String expression) {
            boolean opener = expression.endsWith("{");
            boolean closer = expression.startsWith("}");

            if (opener && closer) return EXTENSION;
            if (opener) return OPENER;
            if (closer) return CLOSER;
            return SINGLE;
        }
    }

    public record Token(ExpressionType type, boolean enabled) {
    }
}