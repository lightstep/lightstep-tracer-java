
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Simple {


    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Simple example...");

        final Tracer tracer = new com.lightstep.tracer.jre.JRETracer(
            new com.lightstep.tracer.shared.Options("{your_access_token}")
                .withVerbosity(4)
        );

        // Create an outer span to capture all activity
        final Span parentSpan = tracer.buildSpan("outer_span").start();
        parentSpan.log("Starting outer span", null);

        // Create a simple child span
        Span childSpan = tracer.buildSpan("hello_world")
            .asChildOf(parentSpan.context())
            .withTag("hello", "world")
            .start();
        Thread.sleep(100);
      	// Note that the returned SpanContext is still valid post-finish().
        SpanContext childCtx = childSpan.context();
        childSpan.finish();

	// Throw inject and extract into the mix, even though we aren't making
	// an RPC.
        Span grandchild = createChildViaInjectExtract(tracer, "grandchild", childCtx);
        grandchild.log("grandchild created", null);
        grandchild.finish();

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

    // An ultra-hacky demonstration of inject() and extract() in-process.
    public static Span createChildViaInjectExtract(Tracer tracer, String opName, SpanContext parentCtx) {
      final Map<String,String> textMap = new HashMap<String,String>();
      final TextMap demoCarrier = new TextMap() {
        public void put(String key, String value) {
          textMap.put(key, value);
        }
        public Iterator<Map.Entry<String,String>> getEntries() {
          return textMap.entrySet().iterator();
        }
      };

      tracer.inject(parentCtx, Format.Builtin.TEXT_MAP, demoCarrier);
      System.out.println("Carrier contents:");
        for (Map.Entry<String,String> entry : textMap.entrySet()) {
          System.out.println(
              "    key='" + entry.getKey() +
              "', value='" + entry.getValue() + "'");
        }
      SpanContext extracted = tracer.extract(Format.Builtin.TEXT_MAP, demoCarrier);
      return tracer.buildSpan(opName).asChildOf(extracted).start();
    }

    public static void spawnWorkers(final Tracer tracer, Span outerSpan) throws InterruptedException  {
        final Span parentSpan = tracer.buildSpan("spawn_workers")
            .asChildOf(outerSpan.context())
            .start();

        System.out.println("Launching worker threads.");

        Thread workers[] = new Thread[4];
        workers[0] = new Thread() {
            public void run() {
                Span childSpan = tracer.buildSpan("worker0")
                    .asChildOf(parentSpan.context())
                    .start();
                for (int i = 0; i < 20; i++) {
                    Span innerSpan = tracer.buildSpan("worker0/microspan")
                        .asChildOf(childSpan.context())
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
                    .asChildOf(parentSpan.context())
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
                    .asChildOf(parentSpan.context())
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
                    .asChildOf(parentSpan.context())
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
