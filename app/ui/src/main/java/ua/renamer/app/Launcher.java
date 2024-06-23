package ua.renamer.app;

public class Launcher {

    public static void main(String[] args) {
        // This launcher is needed to run JavaFX app after build it into jar.
        // https://stackoverflow.com/questions/57019143/build-executable-jar-with-javafx11-from-maven
        // the jar needs to know the actual Main class that does not extend Application,
        // so I just created another Main class called SuperMain (it was only a temporary name)
        // that calls my original main class, which is Main
        RenamerApplication.main(args);
    }
}
