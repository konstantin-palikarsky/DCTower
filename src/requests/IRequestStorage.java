package requests;

import requests.model.Request;

/**
 * This is the data structure that enables the producer - consumer handoff.
 * A wrapping interface reduces coupling allowing us to swap out the underlying implementation.
 * It also allows us to prune unneeded effects of the underlying implementation.
 */
public interface IRequestStorage {

    /**
     * Writes a request that has to be consumed, this function shouldn't block
     * as there doesn't need to be a limit to the amount of the requests
     * that can be marked for processing
     *
     * @param request the request that has to be processed by the elevator system
     */
    void add(Request request);

    /**
     * Consumes a request, blocking until one can be taken for processing
     *
     * @return a request from the data structure
     * @throws InterruptedException if the thread which is currently
     *                              polling for requests gets interrupted
     */
    Request take() throws InterruptedException;

    /**
     * Checks if all data entries added to this data
     * structure have been processed
     *
     * @return true if the data structure contains no requests
     */
    boolean isCurrentlyProcessed();

    /**
     * Finds the number of entries left to be processed in the data structure
     *
     * @return the number of found entries
     */
    int getRequestCount();
}

