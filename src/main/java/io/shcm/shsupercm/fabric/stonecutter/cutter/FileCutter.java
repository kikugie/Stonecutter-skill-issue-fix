package io.shcm.shsupercm.fabric.stonecutter.cutter;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Deque;
import java.util.LinkedList;

public class FileCutter {
    private final File file;
    private final StonecutterTask stonecutter;

    public FileCutter(File file, StonecutterTask stonecutter) {
        this.file = file;
        this.stonecutter = stonecutter;
    }

    public void write(File outputFile) throws Exception {
        StringBuilder transformedContents = new StringBuilder();

        try (Reader oldContents = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            applyVersionedCodeComments(oldContents, transformedContents);
            stonecutter.tokenRemapper().apply(file, transformedContents);
        }

        outputFile.delete();
        Files.writeString(outputFile.toPath(), transformedContents, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    private void applyVersionedCodeComments(Reader input, StringBuilder output) throws StonecutterSyntaxException, IOException {
        Deque<Boolean> conditions = new LinkedList<>();
        while (getExpressionStart(input, output) != null) {
            String expression = getExpressionEnd(input, output);
            if (expression.startsWith("$token"))
                continue;

            Boolean closedState = null;
            final boolean skip;

            if (expression.startsWith("}")) {
                if (conditions.isEmpty())
                    throw new StonecutterSyntaxException("Unexpected } symbol");

                skip = (closedState = conditions.pop()) == null;
                expression = expression.substring(1).stripLeading();
            } else
                skip = false;

            if (!expression.isBlank()) {
                if (expression.endsWith("{"))
                    expression = expression.substring(0, expression.length() - 1).stripTrailing();
                else
                    throw new StonecutterSyntaxException("Expected { symbol");

                if ((closedState != null && closedState) || ((skip || !conditions.isEmpty()) && (conditions.peek() == null || !conditions.peek()))) {
                    conditions.push(null);
                } else {
                    boolean conditionResult = true;
                    if (expression.startsWith("else"))
                        expression = expression.substring(4).stripLeading();
                    if (!expression.isBlank())
                        conditionResult = stonecutter.testVersion(expression);

                    conditions.push(conditionResult);
                }

                // skip 2 only if "/*" is next
                input.mark(2);
                if (input.read() != '/' || input.read() != '*')
                    input.reset();

                if (conditions.peek() == null || !conditions.peek())
                    output.append("/*");
            }
        }
    }

    private static @Nullable String getExpressionStart(Reader input, StringBuilder output) throws IOException {
        return read("/*?", input, output);
    }

    private static String getExpressionEnd(Reader input, StringBuilder output) throws StonecutterSyntaxException, IOException {
        String expression = read("?*/", input, output);
        if (expression == null)
            throw new StonecutterSyntaxException("Expected ?*/ to close stonecutter expression");
        return expression.trim();
    }

    private static String read(String match, Reader input, StringBuilder output) throws IOException {
        StringBuilder substring = new StringBuilder();

        int current;

        while ((current = input.read()) != -1) {
            char ch = (char) current;
            substring.append(ch);
            output.append(ch);

            if (substring.toString().endsWith(match))
                return substring.substring(0, substring.length() - match.length());
        }

        return null;
    }
}
