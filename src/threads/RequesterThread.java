package threads;

import requests.IRequestStorage;
import requests.model.Request;
import requests.model.TravelDirection;

import java.util.concurrent.ExecutorService;

/**
 * This class provides a separate producer thread,
 * whose job it is to supply the consumer (DispatcherThread)
 * with a sufficient amount of fully random lift requests.
 * While this logic could be simplified to a method call within the DcTowerSimulation thread,
 * this implementation allows us to leverage a scheduled thread pool,
 * so that we can have more control over the rate at which requests are sent to the consumer,
 * rather than just the amount of requests.
 */
public class RequesterThread extends Thread {
    private final int maximumRequestCount;
    private int requestCount = 0;

    private final IRequestStorage requests;
    private final ExecutorService threadPool;

    public RequesterThread(IRequestStorage requests, ExecutorService threadPool, int maximumRequestCount) {
        this.requests = requests;
        this.threadPool = threadPool;
        this.maximumRequestCount = maximumRequestCount;
    }

    @Override
    public void run() {
        if (requestCount >= maximumRequestCount) {

            /*
             * Since we are using a scheduled executor service
             * this thread does not need a loop for its run method logic
             * to be executed multiple times, this also means that there
             * is no loop to break out of, so instead we pass the reference
             * of the executor service that runs this thread, and allow the
             * thread to terminate the execution whenever it decides to */
            threadPool.shutdown();
            System.err.println("Request generation complete, shutting down thread pool");
            return;
        }

        requests.add(randomRequest());
        requestCount++;
    }

    private Request randomRequest() {
        double direction = Math.random();
        int randomFloor = (int) (Math.random() * 54) + 1;

        if (direction >= 0.5) {
            return new Request(0, randomFloor, TravelDirection.UP);
        } else {
            return new Request(randomFloor, 0, TravelDirection.DOWN);
        }
    }

}
