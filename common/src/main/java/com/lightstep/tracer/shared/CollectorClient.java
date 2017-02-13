package com.lightstep.tracer.shared;

import com.google.protobuf.ByteString;
import com.lightstep.tracer.grpc.Span;
import com.lightstep.tracer.grpc.CollectorServiceGrpc;
import com.lightstep.tracer.grpc.CollectorServiceGrpc.CollectorServiceBlockingStub;
import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CollectorClient {

  private final ManagedChannel channel;
  private final CollectorServiceBlockingStub blockingStub;
  private ClientMetrics clientMetrics;
  private AbstractTracer tracer;

  /**
   * Constructor client for accessing CollectorService at {@code host:port}
   */
  public CollectorClient(AbstractTracer tracer, String host, int port) {
    this(tracer, ManagedChannelBuilder.forAddress(host, port));
  }

  /**
   * Constructor client for accessing CollectorService using the existing channel
   */
  public CollectorClient(AbstractTracer tracer, ManagedChannelBuilder<?> channelBuilder) {
    this.tracer = tracer;
    channel = channelBuilder.build();
    blockingStub = CollectorServiceGrpc.newBlockingStub(channel);
    clientMetrics = new ClientMetrics(Util.epochTimeMicrosToProtoTime(Util.nowMicrosApproximate()));
  }

  public void shutdown() {
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
    if (clientMetrics != null) {
      reqBuilder.setInternalMetrics(clientMetrics.toGrpc());
    }
    // reset client metrics
    clientMetrics = new ClientMetrics(Util.epochTimeMicrosToProtoTime(Util.nowMicrosApproximate()));

    // send report to collector
    boolean success = false;
    try {
      resp = blockingStub.report(reqBuilder.build());

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

  public synchronized void dropSpan() {
    clientMetrics.spansDropped++;
  }

  public synchronized void dropSpans(List<Span> spans) {
    clientMetrics.spansDropped += spans.size();
  }

  public ClientMetrics getClientMetrics() {
    return clientMetrics;
  }
}
