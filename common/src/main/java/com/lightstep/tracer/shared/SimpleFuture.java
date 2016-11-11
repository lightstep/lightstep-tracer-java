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
        this.resolved = false;
    }

    public SimpleFuture(T value) {
        this.value = value;
        this.resolved = true;
    }

    public void set(T value) {
        synchronized (this) {
            this.value = value;
            this.resolved = true;
            this.notifyAll();
        }
    }

    public T get() throws InterruptedException {
        if (!resolved) {
            synchronized (this) {
                this.wait();
            }
        }
        return this.value;
    }

    @SuppressWarnings("unused")
    public T getWithTimeout(long millis) throws InterruptedException {
        if (!resolved) {
            synchronized (this) {
                this.wait(millis);
            }
        }
        return this.value;
    }
}