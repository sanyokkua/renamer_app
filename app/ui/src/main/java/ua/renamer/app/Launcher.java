package ua.renamer.app;

public class Launcher {

    static void main(String[] args) {
        // Required to launch a JavaFX application from an executable jar.
        // The manifest must reference a class that does not extend Application.
        // https://stackoverflow.com/questions/57019143/build-executable-jar-with-javafx11-from-maven
        RenamerApplication.main(args);
    }
}
