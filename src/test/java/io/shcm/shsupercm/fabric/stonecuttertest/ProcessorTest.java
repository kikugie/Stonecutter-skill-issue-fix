package io.shcm.shsupercm.fabric.stonecuttertest;

import io.shcm.shsupercm.fabric.stonecutter.processor.CommentProcessor;
import io.shcm.shsupercm.fabric.stonecutter.processor.ExpressionProcessor;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ProcessorTest {
    private static void processDirectory(String dir) {
        try {
            Reader sample = getResource(dir + "/sample.txt");
            String expected = getResource(dir + "/expected.txt").lines().reduce("", (acc, line) -> acc + line + "\r\n");

            String result = CommentProcessor.process(sample, ExpressionProcessor.TEST).toString();
            assertEquals(expected.trim(), result.trim());
        } catch (Exception e) {
            fail(e);
        }
    }

    private static BufferedReader getResource(String filename) {
        InputStream is = ProcessorTest.class.getClassLoader().getResourceAsStream(filename);
        assert is != null;
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    @Test
    public void testSingle() {
        processDirectory("single");
    }

    @Test
    public void testBasicMultiline() {
        processDirectory("basicmultiline");
    }
    @Test
    public void testBasicElse() {
        processDirectory("basicelse");
    }

    @Test
    public void testIfElseChain() {
        processDirectory("ifelsechain");
    }

    @Test
    public void testNewSyntax() {
        processDirectory("newsyntax");
    }
}