package requests;

import requests.model.Request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class RequestQueue implements IRequestStorage {
    private final BlockingQueue<Request> tokenQueue;

    public RequestQueue() {
        this.tokenQueue = new LinkedBlockingDeque<>();
    }

    /**
     * Add shouldn't ever block
     */
    public void add(Request request) {
        try {
            tokenQueue.put(request);
        } catch (InterruptedException e) {
            throw new RuntimeException("Request queue interrupted while saving request.");
        }
    }

    public Request take() throws InterruptedException {
        return tokenQueue.take();
    }

    @Override
    public boolean isCurrentlyProcessed() {
        return tokenQueue.isEmpty();
    }

    @Override
    public int getRequestCount() {
        return tokenQueue.size();
    }

    @Override
    public String toString() {
        return "Requests list: " + tokenQueue;
    }
}
