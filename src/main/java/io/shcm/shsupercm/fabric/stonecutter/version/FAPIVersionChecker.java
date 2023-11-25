package io.shcm.shsupercm.fabric.stonecutter.version;

import org.gradle.api.Project;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Predicate;

public class FAPIVersionChecker implements VersionChecker {
    private final Method semverParse, predicateParse;

    public FAPIVersionChecker(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> semverClass = classLoader.loadClass("net.fabricmc.loader.api.SemanticVersion");
        this.semverParse = semverClass.getDeclaredMethod("parse", String.class);
        this.predicateParse = classLoader.loadClass("net.fabricmc.loader.api.metadata.version.VersionPredicate").getDeclaredMethod("parse", String.class);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static VersionChecker create(Project project) {
        File loaderCopy = new File(project.getRootDir(), ".gradle/stonecutter");
        loaderCopy.mkdirs();
        loaderCopy = new File(loaderCopy, "fabric-loader.jar");

        if (loaderCopy.exists())
            try {
                return new FAPIVersionChecker(new URLClassLoader(new URL[]{loaderCopy.toURI().toURL()}, VersionChecker.class.getClassLoader()));
            } catch (Exception ignored) {
            }

        project.getLogger().error("Could not create default fabric loader api version checker!");
        return new VersionChecker() {
            @Override
            public Object parseVersion(String versionString) throws Exception {
                return versionString;
            }

            @Override
            public Predicate<Object> parseChecker(String predicateString) throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Object parseVersion(String versionString) throws Exception {
        return this.semverParse.invoke(null, versionString);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate<Object> parseChecker(String predicateString) throws Exception {
        return (Predicate<Object>) this.predicateParse.invoke(null, predicateString);
    }
}