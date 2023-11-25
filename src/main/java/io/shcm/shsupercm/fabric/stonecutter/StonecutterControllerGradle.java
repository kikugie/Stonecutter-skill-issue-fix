package io.shcm.shsupercm.fabric.stonecutter;

import io.shcm.shsupercm.fabric.stonecutter.cutter.StonecutterTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class StonecutterControllerGradle {
    public final Class<ChiseledTask> chiseled = ChiseledTask.class;
    private final StonecutterProjectSetups.Setup setup;

    public StonecutterControllerGradle(Project project) {
        setup = project.getGradle().getExtensions().getByType(StonecutterProjectSetups.class).get(project);

        for (String version : setup.versions())
            project.project(version).getPluginManager().apply(StonecutterPlugin.class);

        project.getTasks().create("chiseledStonecutter", task -> {
            for (String version : setup.versions())
                task.dependsOn(version + ":setupChiseledStonecutterBuild");
        });

        project.afterEvaluate(afterEvaluate -> {
            StonecutterBuildGradle.Version currentVersionData = afterEvaluate.project(setup.current()).getExtensions().getByType(StonecutterBuildGradle.class).current();
            for (String version : setup.versions()) {
                Project versionProject = afterEvaluate.project(version);
                StonecutterBuildGradle.Version versionData = versionProject.getExtensions().getByType(StonecutterBuildGradle.class).current();
                StonecutterTask task = afterEvaluate.getTasks().create("Set active version to " + versionData.version(), StonecutterTask.class);
                task.setGroup("stonecutter");

                task.getDebug().set(setup.isDebug());
                task.getFromVersion().set(currentVersionData);
                task.getToVersion().set(versionData);
                task.getInputDir().set(afterEvaluate.file("./src"));
                task.getOutputDir().set(task.getInputDir().get());

                task.doLast(taskRun -> {
                    try {
                        File stonecutterGradle = taskRun.getProject().getBuildFile();
                        List<String> stonecutterGradleLines = new ArrayList<>(Files.readAllLines(stonecutterGradle.toPath(), StandardCharsets.ISO_8859_1));

                        stonecutterGradleLines.set(1, "stonecutter.active '" + versionData.version() + "'");

                        stonecutterGradle.delete();
                        Files.write(stonecutterGradle.toPath(), stonecutterGradleLines, StandardCharsets.ISO_8859_1, StandardOpenOption.CREATE);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    public void debug(boolean value) {
        setup.setDebug(value);
    }

    public void active(String current) {
        setup.setCurrent(current);
    }

    public Iterable<String> versions() {
        return setup.versions();
    }

    public void registerChiseled(TaskProvider<?> registeredTask) {
        setup.registerChiseled(registeredTask.getName());
    }
}
