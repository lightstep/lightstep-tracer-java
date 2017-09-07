package com.lightstep.tracer.shared;

import com.google.protobuf.ByteString;
import com.lightstep.tracer.grpc.CollectorServiceGrpc;
import com.lightstep.tracer.grpc.CollectorServiceGrpc.CollectorServiceBlockingStub;
import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;
import com.lightstep.tracer.grpc.Span;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CollectorClient {

  private final ManagedChannelBuilder<?> channelBuilder;
  private ManagedChannel channel;
  private CollectorServiceBlockingStub blockingStub;
  private final ClientMetrics clientMetrics;
  private final AbstractTracer tracer;
  private final long deadlineMillis;

  /**
   * Constructor client for accessing CollectorService using the existing channel
   */
  public CollectorClient(AbstractTracer tracer, ManagedChannelBuilder<?> channelBuilder, long deadlineMillis) {
    this.tracer = tracer;
    this.channelBuilder = channelBuilder;
    this.deadlineMillis = deadlineMillis;
    clientMetrics = new ClientMetrics(Util.epochTimeMicrosToProtoTime(Util.nowMicrosApproximate()));
  }

  public synchronized void shutdown() {
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      tracer.warn("Interrupted while shutting down client, force shutdown now!");
      channel.shutdownNow();
    }
  }

  /**
   * Blocking call to report
   */
  public synchronized ReportResponse report(ReportRequest.Builder reqBuilder) {
    ReportResponse resp = null;
    reqBuilder.setInternalMetrics(clientMetrics.toGrpcAndReset());

    // send report to collector
    boolean success = false;
    try {
      resp = blockingStub.
              withDeadlineAfter(deadlineMillis, TimeUnit.MILLISECONDS).
              report(reqBuilder.build());

      // check response for errors
      if (resp.getErrorsCount() != 0) {
        List<ByteString> errs = resp.getErrorsList().asByteStringList();
        for (ByteString err : errs) {
          tracer.error("Collector response contained error: ", err.toString());
        }
      } else {
        success = true;
      }
    } catch (StatusRuntimeException e) {
      tracer.error("Status runtime exception (likely malformed spans): ", e);
    } catch (RuntimeException e) {
      tracer.error("Runtime exception: ", e);
    } catch (Exception e) {
      tracer.error("Exception sending report to collector: ", e);
    } finally {
      if (!success) {
        dropSpans(reqBuilder.getSpansList());
      }
    }
    return resp;
  }

  private synchronized void connect() {
    channel = channelBuilder.build();
    blockingStub = CollectorServiceGrpc.newBlockingStub(channel);
  }

  synchronized void reconnect() {
    this.shutdown();
    connect();
  }

  void dropSpan() {
    clientMetrics.dropSpans(1);
  }

  private void dropSpans(List<Span> spans) {
    clientMetrics.dropSpans(spans.size());
  }

  ClientMetrics getClientMetrics() {
    return clientMetrics;
  }
}
