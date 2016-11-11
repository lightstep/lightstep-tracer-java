package com.lightstep.tracer.shared;

/**
 * Simple Future/Promise-like class.
 *
 * Acts as a simple wrapper on Object wait() / notify() - avoiding the
 * complex future interfaces in Java 7/8.
 */
public class SimpleFuture<T> {
    private boolean resolved;
    private T value;

    public SimpleFuture() {
        resolved = false;
    }

    public SimpleFuture(T value) {
        this.value = value;
        resolved = true;
    }

    public void set(T value) {
        synchronized (this) {
            this.value = value;
            resolved = true;
            notifyAll();
        }
    }

    public T get() throws InterruptedException {
        if (!resolved) {
            synchronized (this) {
                wait();
            }
        }
        return value;
    }

    @SuppressWarnings("unused")
    public T getWithTimeout(long millis) throws InterruptedException {
        if (!resolved) {
            synchronized (this) {
                wait(millis);
            }
        }
        return value;
    }
}