package com.lightstep.tracer.jre;

import com.lightstep.tracer.shared.Options;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LightStepTracerFactory implements TracerFactory {
    private static final Logger logger = Logger.getLogger(LightStepTracerFactory.class.getName());

    @Override
    public Tracer getTracer()
    {
        Options.OptionsBuilder optsBuilder = TracerParameters.getOptionsFromParameters(createOptionsBuilder());
        if (optsBuilder == null) {
            logger.log(Level.WARNING, "No ls.accessToken value was provided, not trying to initialize the LightStep Tracer");
            return null;
        }

        Options opts;
        JRETracer tracer = null;

        // Although MalformedURLException is the only expected Exception,
        // in practice a few RuntimeException-children can show up.
        try {
            opts = optsBuilder.build();
            tracer = new JRETracer(opts);
            logger.log(Level.INFO, "Created LightStep Tracer: " + tracer);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create a LightStep Tracer instance: " + e);
            return null;
        }

        return tracer;
    }

    protected Options.OptionsBuilder createOptionsBuilder() {
        return new Options.OptionsBuilder();
    }
}
