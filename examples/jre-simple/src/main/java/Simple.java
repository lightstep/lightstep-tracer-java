
import io.opentracing.Span;
import io.opentracing.Tracer;
import com.lightstep.tracer.jre.JRETracer;
import com.lightstep.tracer.shared.*;

public class Simple {
    public static void main(String[] args) {
        System.out.println("Starting Simple example...");

        Tracer tracer = new JRETracer(
            new Options("{your Lightstep access token}")
        );

        Span span = tracer.buildSpan("test_span").start();
        // is null ok to pass in for payload?
        span.log("Hello", null);

        Span childSpan = tracer.buildSpan("test_child_span").withParent(span).withTag("hi", "bye").start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        childSpan.finish();

        span.log("World!", null);
        span.close();

        JRETracer lsTracer = (JRETracer)tracer;
        lsTracer.flush();

        // TODO: this is a terrible hack to ensure the flush finishes before the
        // process is interrupted
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}

        System.out.println("Done!");
    }
}
