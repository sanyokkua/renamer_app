package ua.renamer.app.core.service.mapper;

/**
 * A generic interface for mapping data from one type to another.
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public interface DataMapper<I, O> {

    /**
     * Maps the given input of type I to an output of type O.
     *
     * @param input the input data to be mapped
     *
     * @return the mapped output data
     */
    O map(I input);

}
