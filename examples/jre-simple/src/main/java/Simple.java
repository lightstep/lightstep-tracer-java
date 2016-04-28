import com.lightstep.tracer.Tracer;
import com.lightstep.tracer.Span;

public class Simple {
    public static void main(String[] args) {
        System.out.println("Starting Simple example...");

        Tracer tracer = new com.lightstep.tracer.jre.JRETracer(
            new com.lightstep.tracer.shared.Options("{your_access_token}"));

        Span span = tracer.buildSpan("test_span").start();
        span.finish();

        com.lightstep.tracer.jre.JRETracer lsTracer = (com.lightstep.tracer.jre.JRETracer)tracer;
        lsTracer.flush();

        // TODO: this is a terrible hack to ensure the flush finishes before the
        // process is interrupted
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {}

        System.out.println("Done!");
    }
}
