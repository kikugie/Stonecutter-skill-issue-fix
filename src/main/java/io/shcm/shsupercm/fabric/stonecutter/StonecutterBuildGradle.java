package io.shcm.shsupercm.fabric.stonecutter;

import groovy.lang.MissingPropertyException;
import io.shcm.shsupercm.fabric.stonecutter.cutter.StoneRegexTokenizer;
import io.shcm.shsupercm.fabric.stonecutter.cutter.StonecutterTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class StonecutterBuildGradle {
    private final Project project;
    private final StonecutterProjectSetups.Setup setup;
    private final Version version;

    public StonecutterBuildGradle(Project project) {
        this.project = project;
        this.setup = project.getGradle().getExtensions().getByType(StonecutterProjectSetups.class).get(Objects.requireNonNull(project.getParent()));
        this.version = new Version(this, project.getName());

        project.getTasks().register("setupChiseledStonecutterBuild", StonecutterTask.class, task -> {
            task.getFromVersion().set(project.getParent().project(setup.current()).getExtensions().getByType(StonecutterBuildGradle.class).current());
            task.getToVersion().set(version);
            task.getInputDir().set(project.getParent().file("./src"));
            task.getOutputDir().set(new File(project.getBuildDir(), "chiseledSrc"));
        });

        project.afterEvaluate(this::afterEvaluate);
    }

    private void afterEvaluate(Project project) {
        File loaderCopy = new File(project.getRootDir(), ".gradle/stonecutter");
        loaderCopy.mkdirs();
        loaderCopy = new File(loaderCopy, "fabric-loader.jar");
        if (!loaderCopy.exists())
            loaderSearch: for (Configuration configuration : project.getConfigurations())
                for (Dependency dependency : configuration.getDependencies())
                    if ("net.fabricmc".equals(dependency.getGroup()) && "fabric-loader".equals(dependency.getName()))
                        for (File file : configuration.getFiles())
                            if (file.getName().startsWith("fabric-loader")) {
                                try {
                                    Files.copy(file.toPath(), loaderCopy.toPath());
                                    break loaderSearch;
                                } catch (Exception ignored) { }
                            }


        try {
            if (setup.anyChiseled(project.getGradle().getStartParameter().getTaskNames())) {
                for (SourceSet sourceSet : (SourceSetContainer) Objects.requireNonNull(project.property("sourceSets"))) {
                    sourceSet.getJava().srcDir(new File(project.getBuildDir(), "chiseledSrc/" + sourceSet.getName() + "/java"));
                    sourceSet.getResources().srcDir(new File(project.getBuildDir(), "chiseledSrc/" + sourceSet.getName() + "/resources"));

                    sourceSet.getJava().srcDir(new File(project.getBuildDir(), "chiseledSrc/" + sourceSet.getName() + "/kotlin"));
                }
                return;
            }

            if (this.version.isActiveVersion()) {
                for (SourceSet sourceSet : (SourceSetContainer) Objects.requireNonNull(project.property("sourceSets"))) {
                    sourceSet.getJava().srcDir("../../src/" + sourceSet.getName() + "/java");
                    sourceSet.getResources().srcDir("../../src/" + sourceSet.getName() + "/resources");

                    sourceSet.getJava().srcDir("../../src/" + sourceSet.getName() + "/kotlin");
                }
            }
        } catch (MissingPropertyException ignored) { }
    }

    public Version current() {
        return this.version;
    }

    public String active() {
        return setup.current();
    }

    public Iterable<String> versions() {
        return setup.versions();
    }

    public static class Version {
        private final StonecutterBuildGradle plugin;
        private final String version;
        private StoneRegexTokenizer tokenizer = null;

        public Version(StonecutterBuildGradle plugin, String version) {
            this.plugin = plugin;
            this.version = version;
        }

        public String version() {
            return this.version;
        }

        public boolean isActiveVersion() {
            return this.version.equals(plugin.setup.current());
        }

        public Project project() {
            return this.plugin.project;
        }

        public StoneRegexTokenizer tokenizer() {
            if (this.tokenizer == null) {
                this.tokenizer = new StoneRegexTokenizer();
                final File tokensFile = project().file("tokens.gradle");
                if (tokensFile.exists())
                    project().apply(it -> {
                        it.from(tokensFile);
                        it.to(this.tokenizer);
                    });
            }

            return this.tokenizer;
        }
    }
}
