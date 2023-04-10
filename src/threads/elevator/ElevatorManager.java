package threads.elevator;

import requests.model.Request;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * This class wraps the 7 elevator threads in a way that allows to abstract
 * the handling of individual requests. Instead, it encapsulates the logic
 * for choosing the proper elevator thread, and also the logic that starts
 * and stops the elevator threads. It also provides the waiting methods
 * that the implementation uses to fully resynchronize once the simulation
 * is complete.
 */
public class ElevatorManager {
    private final ArrayList<Elevator> elevators;

    public ElevatorManager(ArrayList<Elevator> elevators) {
        this.elevators = elevators;
    }

    public void startElevators() {
        for (Elevator elevator : elevators) {
            elevator.start();
        }
    }

    public void shutdown() {
        for (Elevator elevator : elevators) {
            elevator.interrupt();
        }
    }

    /**
     * This method finds the optimal elevator
     * as defined by the algorithm in the
     * private method getOptimalElevator()
     * and hands off the request for execution
     * to the designated optimal elevator thread.
     *
     * @param request the request to be handled by the proper elevator
     */
    public void handleRequest(Request request) {
        var handler = getOptimalElevator(request);
        handler.addRequest(request);
    }

    /**
     * This method returns after
     * every single elevator thread
     * has exited its run() method.
     */
    public void awaitTermination() {
        while (!threadsAreShutdown()) {
        }
    }

    /**
     * This method returns after
     * every single elevator thread
     * has no outstanding requests
     * to handle.
     */
    public void awaitWorkCompletion() {
        while (!tasksAreCompleted()) {
        }
    }

    private synchronized Elevator getOptimalElevator(Request request) {
        var freeElevator = getClosestUnusedElevator(request.currentFloor());

        if (freeElevator != null) {
            return freeElevator;
        }

        return getClosestLeastBusyElevator(request.currentFloor());
    }

    private Elevator getClosestUnusedElevator(int currentFloor) {
        var freeElevators = new ArrayList<>(elevators.stream().filter(Elevator::isFree).toList());
        if (freeElevators.isEmpty()) {
            return null;
        }
        freeElevators
                .sort(Comparator.comparingInt(elevator -> Math.abs(elevator.getFloor() - currentFloor)));
        return freeElevators.get(0);
    }

    private Elevator getClosestLeastBusyElevator(int currentFloor) {
        var sortedElevators = new ArrayList<>(elevators);

        sortedElevators
                .sort(Comparator.comparingInt(Elevator::getRequestCount));

        var leastBusyElevators = new ArrayList<>(
                sortedElevators
                        .stream()
                        .filter(elevator -> elevator.getRequestCount() == sortedElevators.get(0).getRequestCount())
                        .toList());

        if (leastBusyElevators.size() > 1) {
            leastBusyElevators
                    .sort(Comparator.comparingInt(elevator -> Math.abs(elevator.getFinalDestination() - currentFloor)));

            return leastBusyElevators.get(0);
        }

        return leastBusyElevators.get(0);
    }

    private boolean threadsAreShutdown() {
        var complete = true;

        for (Elevator elevator : elevators) {
            complete = complete && elevator.isExited();
        }

        return complete;
    }

    private boolean tasksAreCompleted() {
        var complete = true;

        for (Elevator elevator : elevators) {
            complete = complete && elevator.isFree();
        }

        return complete;
    }
}
