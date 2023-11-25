package io.shcm.shsupercm.fabric.stonecutter.cutter;

import io.shcm.shsupercm.fabric.stonecutter.processor.CommentProcessor;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FileCutter {
    private final File file;
    private final StonecutterTask stonecutter;

    public FileCutter(File file, StonecutterTask stonecutter) {
        this.file = file;
        this.stonecutter = stonecutter;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void write(File outputFile) throws Exception {
        StringBuilder transformedContents;
        try (Reader oldContents = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
            transformedContents = CommentProcessor.process(oldContents, stonecutter.processor());
            stonecutter.tokenRemapper().apply(file, transformedContents);
        }

        outputFile.delete();
        Files.writeString(outputFile.toPath(), transformedContents, StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE);
    }
}
