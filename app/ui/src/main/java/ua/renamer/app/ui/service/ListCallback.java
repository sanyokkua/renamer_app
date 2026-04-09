package ua.renamer.app.ui.service;

import java.util.List;
import java.util.function.Consumer;

@FunctionalInterface
public interface ListCallback<T> extends Consumer<List<T>> {

}
