package com.lightstep.samples.sampleapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lightstep.tracer.shared.Options;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "LightStep Example";

    private Tracer tracer;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize tracer
        this.tracer = new com.lightstep.tracer.android.Tracer(
                getContext(),
                new Options("{your_access_token}").withVerbosity(4));
        Log.d(TAG, "Tracer successfully initialized!");

        View fragmentView = (View) inflater.inflate(R.layout.fragment_main, container, false);
        final EditText usernameView = (EditText) fragmentView.findViewById(R.id.usernameView);
        final TextView resultsView = (TextView) fragmentView.findViewById(R.id.resultsView);
        final ScrollView resultsScrollView = (ScrollView) fragmentView.findViewById(R.id.resultsScrollView);

        Button btn = (Button) fragmentView.findViewById(R.id.button);
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameView.getText().toString();
                final Span span = tracer.buildSpan("getUserGitHubInfo").start();
                progressDialog.setMessage("Getting GitHub info for \"" + username + "\"");
                progressDialog.setProgress(0);
                progressDialog.show();
                (new QueryTask(resultsView, resultsScrollView, progressDialog, username, span)).execute();
            }
        });

        return fragmentView;
    }

    class QueryTask extends AsyncTask<Void, Void, Void> {
        private final RequestQueue queue = Volley.newRequestQueue(getContext());
        private final TextView resultsView;
        private final ScrollView resultsScrollView;
        private final ProgressDialog progressDialog;
        private final String username;
        /**
         * The span that this asynchronous task will finish.
         */
        private final Span span;
        private JSONObject user;
        private String reposResponse, eventsResponse;

        QueryTask(TextView resultsView, ScrollView resultsScrollView, ProgressDialog progressDialog,
                  String username, Span span) {
            this.resultsView = resultsView;
            this.resultsScrollView = resultsScrollView;
            this.progressDialog = progressDialog;
            this.username = username;
            this.span = span;
        }

        // TODO really, handle errors in a reasonable way
        @Override
        protected Void doInBackground(Void... voids) {
            final Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    span.finish();
                    displayError(error.toString());
                }
            };
            try {
                doHTTPGet("https://api.github.com/users/" + username,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    user = new JSONObject(response);
                                    doHTTPGet(user.getString("repos_url"),
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    synchronized (QueryTask.this) {
                                                        reposResponse = response;
                                                        if (eventsResponse != null) {
                                                            displayResults();
                                                        }
                                                    }
                                                }
                                            }, errorListener);
                                    doHTTPGet(user.getString("received_events_url"),
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    synchronized (QueryTask.this) {
                                                        eventsResponse = response;
                                                        if (reposResponse != null) {
                                                            displayResults();
                                                        }
                                                    }
                                                }
                                            }, errorListener);
                                } catch (JSONException e) {
                                    span.log("Error parsing JSON response", e);
                                    span.setTag("error", true);
                                    span.finish();
                                    displayError(e.toString());
                                }
                            }
                        }, errorListener);
                return null;
            } catch (Exception e) {
                span.log("Caught exception", e);
                span.setTag("error", true);
                span.finish();
                displayError(e.toString());
                throw e;
            }
        }

        private void doHTTPGet(String url,
                               final Response.Listener<String> listener,
                               final Response.ErrorListener errorListener) {
            Span childSpan = tracer.buildSpan("http_get").asChildOf(span).start();
            childSpan.log("HTTP request", url);
            WrappedRequest req = new WrappedRequest(childSpan, url, listener, errorListener);
            queue.add(req);
        }

        private void displayResults() {
            progressDialog.hide();
            try {
                StringBuilder output = new StringBuilder();
                output.append("User: ");
                output.append(this.user.getString("login"));
                output.append("\nType: ");
                output.append(this.user.getString("type"));

                JSONArray repos = new JSONArray(this.reposResponse);
                output.append("\nPublic repositories: ");
                output.append(repos.length());
                for (int i = 0; i < repos.length(); i++) {
                    output.append("\n\t");
                    output.append(repos.getJSONObject(i).getString("name"));
                }
                JSONArray events = new JSONArray(this.eventsResponse);
                int eventTotal = 0;
                HashMap<String, Integer> eventCounts = new HashMap<>();
                for (int i = 0; i < events.length(); i++) {
                    String key = events.getJSONObject(i).getString("type");
                    if (eventCounts.containsKey(key)) {
                        eventCounts.put(key, eventCounts.get(key) + 1);
                    } else {
                        eventCounts.put(key, 1);
                    }
                    eventTotal++;
                }
                output.append("\nRecent events: ");
                output.append(eventTotal);
                for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
                    output.append("\n\t");
                    output.append(entry.getKey());
                    output.append(": ");
                    output.append(entry.getValue());
                }
                output.append("\n\nView trace at:\n");
                output.append(((com.lightstep.tracer.shared.Span) span).generateTraceURL());
                resultsView.setText(output.toString());
                resultsScrollView.setScrollY(0);
            } catch (JSONException e) {
                span.log("Error extracting info from JSON response", e);
            } finally {
                span.finish();
            }
        }

        private void displayError(String error) {
            progressDialog.hide();
            StringBuilder output = new StringBuilder();
            output.append("Something went wrong:\n");
            output.append(error);
            output.append("\n\nView trace at:\n");
            output.append(((com.lightstep.tracer.shared.Span) span).generateTraceURL());
            resultsView.setText(output.toString());
            resultsScrollView.setScrollY(0);
        }
    }

    class WrappedRequest extends StringRequest {
        private final com.lightstep.tracer.shared.Span span;

        WrappedRequest(final Span span, String url,
                       final Response.Listener<String> listener,
                       final Response.ErrorListener errorListener) {
            super(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // TODO don't parse twice
                            try {
                                JSONObject obj = new JSONObject(response);
                                span.log("HTTP response", obj);
                            } catch (JSONException e) {
                                // Just log the string
                                span.log("HTTP response", response);
                            }
                            span.finish();
                            // TODO check for application errors
                            listener.onResponse(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO also propagate errors
                            span.log("HTTP error", error);
                            span.finish();
                            errorListener.onErrorResponse(error);
                        }
                    });
            this.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 1.0f));
            this.span = (com.lightstep.tracer.shared.Span) span;
        }

        @Override
        public Map<String, String> getHeaders() {
            com.lightstep.tracer.shared.SpanContext ctxImp = (com.lightstep.tracer.shared.SpanContext) span.context();
            Map<String, String> map = new HashMap<>();
            map.put("LightStep-Trace-GUID", ctxImp.getTraceId());
            map.put("LightStep-Parent-GUID", ctxImp.getSpanId());
            map.put("LightStep-Access-Token", span.getTracer().getAccessToken());
            return map;
        }
    }
}
