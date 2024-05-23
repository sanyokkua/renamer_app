package ua.renamer.app.core.abstracts;

public interface DataMapper<I, O> {

    O map(I input);

}
