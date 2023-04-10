package threads;

import requests.IRequestStorage;
import threads.elevator.ElevatorManager;

/**
 * This class provides the consumer part of our producer-consumer solution.
 * This thread takes requests from the data structure in which they are enqueued
 * and hands them over to the elevator logic for handling.
 */
public class DispatcherThread extends Thread {
    private final ElevatorManager elevators;
    private final IRequestStorage requests;

    public DispatcherThread(ElevatorManager elevators, IRequestStorage requests) {
        this.elevators = elevators;
        this.requests = requests;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                var request = requests.take();
                elevators.handleRequest(request);

            } catch (InterruptedException e) {
                System.err.println("Exiting elevator dispatcher thread");
                break;
            }
        }
    }
}
