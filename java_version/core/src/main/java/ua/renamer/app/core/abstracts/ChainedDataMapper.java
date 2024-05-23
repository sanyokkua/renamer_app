package ua.renamer.app.core.abstracts;

import java.util.Optional;

public interface ChainedDataMapper<I, O> extends DataMapper<I, O> {

    Optional<ChainedDataMapper<I, O>> getNext();
    void setNext(ChainedDataMapper<I, O> next);
    boolean canHandle(I input);
    O process(I input);

}
