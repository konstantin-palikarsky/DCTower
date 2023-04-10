package threads.elevator;

import requests.IRequestStorage;
import requests.model.Request;
import requests.model.TravelDirection;

/**
 * This class contains the actual logic for handling an elevator request
 * it simulates an elevator responding to a request, by updating the
 * state which designates the floor at which an elevator currently
 * resides in a manner which causes a real-time delay, set by the
 * ELEVATOR_MOVEMENT_DELAY_MS field. This allows the simulation
 * to have a more realistic manner of execution, not only logically
 * when deciding which is the optimal elevator, but also physically,
 * as those decisions cause a comparative slowdown by enforcing a time
 * penalty for every floor moved.
 * <p>
 * This implementation assumes that no intermittent stops are possible,
 * and that no stops can be added to a request.
 */
public class Elevator extends Thread {
    private final long elevatorMovementDelayMs;
    private final IRequestStorage assignedRequests;
    private final int elevatorId;

    private int floor;
    private boolean exited = false;
    private boolean busy = false;
    private int finalDestination;

    public Elevator(long elevatorMovementDelayMs, int elevatorId, int floor, IRequestStorage assignedRequests) {
        this.elevatorMovementDelayMs = elevatorMovementDelayMs;
        this.assignedRequests = assignedRequests;
        this.floor = floor;
        this.elevatorId = elevatorId;
    }

    public void addRequest(Request request) {
        assignedRequests.add(request);
        finalDestination = request.destinationFloor();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                var request = assignedRequests.take();
                busy = true;

                if (floor == request.currentFloor()) {
                    System.out.println(
                            "[Lift #" + elevatorId + ", PICKUP, from: " + floor + "]");
                } else {
                    System.out.println(
                            "[Lift #" + elevatorId + ", PICKUP, from: " + request.currentFloor() +
                                    ", request received on floor: " + floor + "]");
                }

                moveTo(request.currentFloor());
                System.out.println(
                        "[Lift #" + elevatorId + ", DELIVERY, from: " + floor + ", to: " + request.destinationFloor() + "]");

                moveTo(request.destinationFloor());

                busy = false;
            } catch (InterruptedException e) {
                System.err.println("Exiting elevator thread #" + elevatorId);
                break;
            }
        }
        exited = true;
    }

    public boolean isExited() {
        return exited;
    }

    public int getFinalDestination() {
        return finalDestination;
    }

    public boolean isFree() {
        return assignedRequests.isCurrentlyProcessed() && !busy;
    }

    public int getFloor() {
        return floor;
    }

    public int getRequestCount() {
        return assignedRequests.getRequestCount();
    }

    private void moveTo(int targetFloor) throws InterruptedException {
        TravelDirection movementDirection;
        if (floor > targetFloor) {
            movementDirection = TravelDirection.DOWN;
        } else {
            movementDirection = TravelDirection.UP;
        }

        while (floor != targetFloor) {
            move(movementDirection);
        }

    }

    private void move(TravelDirection direction) throws InterruptedException {
        switch (direction) {
            case UP -> {
                sleep(elevatorMovementDelayMs);
                floor++;
            }
            case DOWN -> {
                sleep(elevatorMovementDelayMs);
                floor--;
            }
            default -> throw new IllegalArgumentException("Travel direction can only be up or down");
        }
    }

}
