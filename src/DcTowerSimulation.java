import requests.IRequestStorage;
import requests.RequestQueue;
import threads.DispatcherThread;
import threads.RequesterThread;
import threads.elevator.Elevator;
import threads.elevator.ElevatorManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is the main class of the solution to the DC Tower Elevator challenge.
 * The solution I implemented is based on the producer-consumer pattern, with
 * a producer thread and a consumer thread. The program logic is encapsulated
 * in the Elevator class itself, which extends the Java Thread class. The
 * problem of properly scheduling requests for consumption by the optimal
 * elevator is covered by the ElevatorManager class. It also provides
 * a more abstracted manner of controlling the lifecycle of the threads
 * in it.
 */
public class DcTowerSimulation implements Runnable {
    /* Simulation configuration fields */
    private static final int REQUEST_CREATION_PERIOD_MS = 500;
    private static final int MAXIMUM_REQUEST_COUNT = 21;
    private static final int ELEVATOR_MOVEMENT_DELAY_MS = 250;

    private static final String SIMULATION_START_ANNOUNCEMENT = "Simulation starting!" + System.lineSeparator()
            + "A new request for an elevator ride will be submitted every "
            + REQUEST_CREATION_PERIOD_MS + " ms." + System.lineSeparator()
            + "A total of " + MAXIMUM_REQUEST_COUNT + " requests will be submitted." + System.lineSeparator() +
            "Elevators will take " + ELEVATOR_MOVEMENT_DELAY_MS + " ms to move from one floor to the next." +
            System.lineSeparator() + "**********************************************************";

    private final IRequestStorage requests;

    private final ElevatorManager elevators;
    private final ScheduledExecutorService requesterPool = Executors.newSingleThreadScheduledExecutor();
    private DispatcherThread elevatorDispatcher;

    public DcTowerSimulation(IRequestStorage requests, ElevatorManager elevators) {
        this.requests = requests;
        this.elevators = elevators;
    }

    @Override
    public void run() {
        System.out.println(SIMULATION_START_ANNOUNCEMENT);

        elevatorDispatcher = new DispatcherThread(elevators, requests);
        elevatorDispatcher.start();
        elevators.startElevators();

        requesterPool.scheduleAtFixedRate(new RequesterThread(requests, requesterPool, MAXIMUM_REQUEST_COUNT),
                0, REQUEST_CREATION_PERIOD_MS, TimeUnit.MILLISECONDS);

        awaitTaskCompletion();

        shutdown();
    }

    private void shutdown() {
        elevators.shutdown();
        elevatorDispatcher.interrupt();

        elevators.awaitTermination();

        System.out.println("Simulation over!" + System.lineSeparator() +
                "All threads safely interrupted and closed.");
    }

    private void awaitTaskCompletion() {
        var shutdownComplete = false;
        while (!shutdownComplete) {
            try {
                shutdownComplete = requesterPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }

        elevators.awaitWorkCompletion();
    }

    public static void main(String[] args) {
        var elevatorThreadList = new ArrayList<Elevator>();

        for (int i = 0; i < 7; i++) {
            elevatorThreadList.add(new Elevator(ELEVATOR_MOVEMENT_DELAY_MS, i + 1, 0, new RequestQueue()));
        }

        var towerSimulation = new DcTowerSimulation(new RequestQueue(), new ElevatorManager(elevatorThreadList));
        towerSimulation.run();
    }

}