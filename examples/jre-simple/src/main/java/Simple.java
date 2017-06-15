import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.Options;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

public class Simple {
    public static void main(String[] args) throws InterruptedException, MalformedURLException {
        System.out.println("Starting Simple example...");

        Options options = new Options.OptionsBuilder()
                .withAccessToken("{your_access_token}")
                .withComponentName("JRE Simple")
                .withVerbosity(4)
                .build();
        final Tracer tracer = new JRETracer(options);

        // Create a simple span and delay for a while to ensure the reporting
        // loop works as expected
        final Span mySpan = tracer.buildSpan("my_span").start();

        // Play with different sorts of payloads for fun.
        mySpan.log("just a message");
        mySpan.log("just a message");
        mySpan.log("no payload", null);
        mySpan.log("string payload", "str");
        Map<String, Object> m = new HashMap<>();
        m.put("foo", "bar");
        m.put("baz", 42);
        mySpan.log("map payload", m);
        mySpan.log(m);
        m.put("event", "now an event field exists");
        mySpan.log(m);
        mySpan.finish();
        Thread.sleep(4000);

        // Create an outer span to capture all activity
        final Span parentSpan = tracer
                .buildSpan("outer_span")
                .withTag("favorie_unicode", "🌠🍕🍕🍕🍕")
                .withTag("boring_characters", " \n\b\t()%20/\\#@$!-=")
                .withTag("Valid ASCII", "abcdefg")
                .withTag("Manual unicode", "\u0027\u0018\u00f6\u0003\u0012\u008e\u00fa\u00ec\u0011\r")
                .withTag("🍕", "pepperoni")
                .start();
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

        ((com.lightstep.tracer.jre.JRETracer) tracer).flush(20000);
        System.out.println("Done!");
    }

    // An ultra-hacky demonstration of inject() and extract() in-process.
    private static Span createChildViaInjectExtract(Tracer tracer, String opName, SpanContext parentCtx) {
        final Map<String, String> textMap = new HashMap<>();
        final TextMap demoCarrier = new TextMap() {
            public void put(String key, String value) {
                textMap.put(key, value);
            }

            public Iterator<Map.Entry<String, String>> iterator() {
                return textMap.entrySet().iterator();
            }
        };

        tracer.inject(parentCtx, Format.Builtin.TEXT_MAP, demoCarrier);
        System.out.println("Carrier contents:");
        for (Map.Entry<String, String> entry : textMap.entrySet()) {
            System.out.println(
                    "    key='" + entry.getKey() +
                            "', value='" + entry.getValue() + "'");
        }
        SpanContext extracted = tracer.extract(Format.Builtin.TEXT_MAP, demoCarrier);
        return tracer.buildSpan(opName).asChildOf(extracted).start();
    }

    private static void spawnWorkers(final Tracer tracer, Span outerSpan) throws InterruptedException {
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
