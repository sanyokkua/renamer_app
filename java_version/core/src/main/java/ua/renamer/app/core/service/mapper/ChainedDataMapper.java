package ua.renamer.app.core.service.mapper;

import java.util.Optional;

/**
 * A generic interface for chained data mappers, extending the DataMapper interface.
 * Chained data mappers can pass data through a chain of responsibility pattern.
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public interface ChainedDataMapper<I, O> extends DataMapper<I, O> {

    /**
     * Retrieves the next data mapper in the chain.
     *
     * @return an Optional containing the next ChainedDataMapper, or an empty Optional if there is no next mapper
     */
    Optional<ChainedDataMapper<I, O>> getNext();
    /**
     * Sets the next data mapper in the chain.
     *
     * @param next the next ChainedDataMapper in the chain
     */
    void setNext(ChainedDataMapper<I, O> next);
    /**
     * Checks if this data mapper can handle the given input.
     *
     * @param input the input data to be checked
     *
     * @return true if this mapper can handle the input, false otherwise
     */
    boolean canHandle(I input);
    /**
     * Processes the given input data and returns the output.
     * This method should be implemented to define the actual mapping logic.
     *
     * @param input the input data to be processed
     *
     * @return the processed output data
     */
    O process(I input);

}
