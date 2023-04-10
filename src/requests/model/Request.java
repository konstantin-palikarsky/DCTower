package requests.model;

public record Request(int currentFloor, int destinationFloor, TravelDirection direction) {
    /*
     * While the travel direction can be inferred from the current and destination floors,
     * encapsulating it alongside the other request parameters spares us a lot of computation,
     * and allows for cleaner case distinctions.
     * */
}
