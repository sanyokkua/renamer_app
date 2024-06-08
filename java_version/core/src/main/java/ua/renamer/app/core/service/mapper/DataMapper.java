package ua.renamer.app.core.service.mapper;

public interface DataMapper<I, O> {

    O map(I input);

}
