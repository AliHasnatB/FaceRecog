package com.backex.faceox;

/**
 * Plain (non-JavaFX) entry point.
 *
 * WHY THIS EXISTS:
 * When a fat-jar's Main-Class directly extends javafx.application.Application,
 * the JVM's launcher checks for JavaFX on the boot class-path BEFORE main() runs
 * and throws "JavaFX runtime components are missing" even though the JavaFX jars
 * are inside the fat-jar.
 *
 * The fix: put Main-Class in a class that does NOT extend Application.
 * This class just delegates to MainApp.main(), which does extend Application.
 * By the time the JVM gets there, the classpath is fully loaded.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
