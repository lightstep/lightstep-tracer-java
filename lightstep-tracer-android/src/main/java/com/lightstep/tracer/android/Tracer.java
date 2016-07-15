package com.lightstep.tracer.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.thrift.KeyValue;

public class Tracer extends AbstractTracer {
  private final Context ctx;

  private static final String TAG = "Tracer";

  /**
   * Create a new tracer that will send spans to a LightStep collector.
   *
   * @param ctx the current context
   * @param options control LightStep-specific behavior
   */
  public Tracer(Context ctx, Options options) {
    super(options);

    this.ctx = ctx;

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
        new KeyValue(super.COMPONENT_NAME_KEY,
                     ctx.getApplicationInfo().processName));
    }
  }

  /**
   * Flush any buffered data.
   */
  @Override
  public void flush() {
    synchronized(this.mutex) {
      if (this.isDisabled || this.ctx == null) {
        return;
      }

      ConnectivityManager connMgr = (ConnectivityManager)
        this.ctx.getApplicationContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
        new AsyncFlush().execute();
      }
    }
  }

  private class AsyncFlush extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void ...voids) {
      sendReport(false);
      return null;
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
