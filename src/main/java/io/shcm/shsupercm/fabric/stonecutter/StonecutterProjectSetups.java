package io.shcm.shsupercm.fabric.stonecutter;

import org.gradle.api.Project;

import java.util.*;

public class StonecutterProjectSetups {
    private final Map<String, Setup> controllerSetups = new HashMap<>();

    protected boolean registerVersioned(String project, StonecutterSettingsGradle.StonecutterProjectBuilder setupBuilder) {
        return this.controllerSetups.putIfAbsent(project, new Setup(setupBuilder)) == null;
    }

    public Setup get(Project project) {
        return this.controllerSetups.get(project.getPath());
    }

    public static class Setup {
        private final String[] versions;
        private final String vcsVersion;
        private String current;
        private final Set<String> chiseledTasks = new HashSet<>();
        private boolean debug = false;

        private Setup(StonecutterSettingsGradle.StonecutterProjectBuilder setupBuilder) {
            this.versions = setupBuilder.versions;
            this.vcsVersion = setupBuilder.vcsVersion;

            this.current = vcsVersion;
        }

        public List<String> versions() {
            return List.of(this.versions);
        }

        public String current() {
            return this.current;
        }

        public void setCurrent(String version) {
            if (!versions().contains(version))
                throw new IllegalArgumentException("Version not registered for project");
            this.current = version;
        }

        public void registerChiseled(String taskName) {
            this.chiseledTasks.add(taskName);
        }

        public boolean anyChiseled(Iterable<String> taskNames) {
            for (String taskName : taskNames)
                if (chiseledTasks.contains(taskName))
                    return true;

            return false;
        }

        public void setDebug(boolean value) {
            this.debug = value;
        }

        public boolean isDebug() {
            return debug;
        }
    }
}
