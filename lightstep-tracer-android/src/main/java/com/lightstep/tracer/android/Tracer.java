package com.lightstep.tracer.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.SimpleFuture;
import com.lightstep.tracer.shared.Version;
import com.lightstep.tracer.thrift.KeyValue;

public class Tracer extends AbstractTracer {
    private final Context ctx;

    private static final String TAG = "Tracer";

    private static final int DEFAULT_REPORTING_INTERVAL_MILLIS = 30 * 1000;

    /**
     * Create a new tracer that will send spans to a LightStep collector.
     *
     * @param ctx     the current context
     * @param options control LightStep-specific behavior
     */
    public Tracer(Context ctx, Options options) {
        super(AbstractTracer.setDefaultReportingIntervalMillis(options, DEFAULT_REPORTING_INTERVAL_MILLIS));

        this.ctx = ctx;
        this.addStandardTracerTags();
    }

    /**
     * Flush any buffered data.
     */
    @Override
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        synchronized (this.mutex) {
            if (this.isDisabled || this.ctx == null) {
                return new SimpleFuture<Boolean>(false);
            }

            SimpleFuture<Boolean> future = new SimpleFuture<Boolean>();

            ConnectivityManager connMgr = (ConnectivityManager)
                    this.ctx.getApplicationContext()
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                AsyncFlush asyncFlush = new AsyncFlush(future, explicitRequest);
                asyncFlush.execute();
            } else {
                future.set(false);
            }
            return future;
        }
    }

    private class AsyncFlush extends AsyncTask<Void, Void, Void> {
        private SimpleFuture<Boolean> future;
        private boolean explicitRequest;

        AsyncFlush(SimpleFuture<Boolean> future, boolean explicitRequest) {
            this.future = future;
            this.explicitRequest = explicitRequest;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            boolean ok = sendReport(this.explicitRequest);
            this.future.set(ok);
            return null;
        }
    }

    /**
     * Adds standard tags set by all LightStep client libraries.
     */
    protected void addStandardTracerTags() {
        // The platform is called "jre" rather than "Java" to clearly
        // differentiate this library from the Android version
        this.addTracerTag(LIGHTSTEP_TRACER_PLATFORM_KEY, "android");
        this.addTracerTag(LIGHTSTEP_TRACER_PLATFORM_VERSION_KEY, String.valueOf(android.os.Build.VERSION.SDK_INT));
        this.addTracerTag(LIGHTSTEP_TRACER_VERSION_KEY, Version.LIGHTSTEP_TRACER_VERSION);

        // Check to see if component name is set and, if not, use the app process
        // or package name.
        boolean found = false;
        for (KeyValue keyValue : super.runtime.attrs) {
            if (keyValue.getKey() == super.COMPONENT_NAME_KEY) {
                found = true;
                break;
            }
        }
        if (!found) {
            super.runtime.addToAttrs(
                    new KeyValue(super.COMPONENT_NAME_KEY, ctx.getApplicationInfo().processName));
        }
    }

    protected void printLogToConsole(InternalLogLevel level, String msg, Object payload) {
        String s = msg;
        if (payload != null) {
            s += " " + payload.toString();
        }
        switch (level) {
            case DEBUG:
                Log.d(TAG, s);
                break;
            case INFO:
                Log.i(TAG, s);
                break;
            case WARN:
                Log.w(TAG, s);
                break;
            case ERROR:
                Log.e(TAG, s);
                break;
            default:
                Log.e(TAG, s);
                break;
        }
    }

    // TODO: perhaps get fields from
    // android.os.Build.class.getDeclaredFields() and use them.
}
