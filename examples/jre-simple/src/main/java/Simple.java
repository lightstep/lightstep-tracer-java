
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Simple {


    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Simple example...");

        final Tracer tracer = new com.lightstep.tracer.jre.JRETracer(
            new com.lightstep.tracer.shared.Options("{your_access_token}")
        );

        // Create an outer span to capture all activity
        Span parentSpan = tracer.buildSpan("outer_span").start();
        parentSpan.log("Starting outer span", null);

        // Create a simple child span
        Span childSpan = tracer.buildSpan("hello_world")
            .withParent(parentSpan)
            .withTag("hello", "world")
            .start();
        Thread.sleep(100);
        childSpan.finish();

        // Spawn some concurrent threads - which in turn will spawn their
        // own worker threads
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            executor.execute(new Thread() {
                public void run() {
                    try {
                        spawnWorkers(tracer, parentSpan);
                    } catch (InterruptedException e) {
                        parentSpan.setTag("error", "true");
                        parentSpan.log("InterruptedException", e);
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);

        parentSpan.finish();
        System.out.println("Done!");
    }

    public static void spawnWorkers(Tracer tracer, Span outerSpan) throws InterruptedException  {
        Span parentSpan = tracer.buildSpan("spawn_workers")
            .withParent(outerSpan)
            .start();

        System.out.println("Launching worker threads.");

        Thread workers[] = new Thread[4];
        workers[0] = new Thread() {
            public void run() {
                Span childSpan = tracer.buildSpan("worker0")
                    .withParent(parentSpan)
                    .start();
                for (int i = 0; i < 20; i++) {
                    Span innerSpan = tracer.buildSpan("worker0/microspan")
                        .withParent(childSpan)
                        .start();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        childSpan.setTag("error", "true");
                        childSpan.log("InterruptedException!", e);
                    }
                    innerSpan.finish();
                }
                childSpan.finish();
            }
        };
        workers[1] = new Thread() {
            public void run() {
                Span childSpan = tracer.buildSpan("worker1")
                    .withParent(parentSpan)
                    .start();
                for (int i = 0; i < 20; i++) {
                    childSpan.log("Beginning inner loop", i);
                    for (int j = 0; j < 10; j++) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            childSpan.setTag("error", "true");
                            childSpan.log("InterruptedException!", e);
                        }
                    }
                }
                childSpan.finish();
            }
        };
        workers[2] = new Thread() {
            public void run() {
                Span childSpan = tracer.buildSpan("worker2")
                    .withParent(parentSpan)
                    .start();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    childSpan.setTag("error", "true");
                    childSpan.log("InterruptedException!", e);
                }
                childSpan.finish();
            }
        };
        workers[3] = new Thread() {
            public void run() {
                Span childSpan = tracer.buildSpan("worker3")
                    .withParent(parentSpan)
                    .start();
                for (int i = 0; i < 20; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        childSpan.setTag("error", "true");
                        childSpan.log("InterruptedException!", e);
                    }
                }
                childSpan.finish();
            }
        };

        for (int i = 0; i < 4; i++) {
            workers[i].start();
        }
        for (int i = 0; i < 4; i++) {
            workers[i].join();
        }
        System.out.println("Finished worker threads.");
        parentSpan.finish();
    }
}
