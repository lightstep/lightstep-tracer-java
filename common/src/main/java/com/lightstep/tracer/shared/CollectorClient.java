package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.CollectorServiceGrpc;
import com.lightstep.tracer.grpc.CollectorServiceGrpc.CollectorServiceBlockingStub;
import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;

public class CollectorClient {

  private final ManagedChannel channel;
  private final CollectorServiceBlockingStub blockingStub;

  /**
   * Constructor client for accessing CollectorService at {@code host:port}
   */
  public CollectorClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  /**
   * Constructor client for accessing CollectorService using the existing channel
   */
  public CollectorClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = CollectorServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void shutdownNow() {
    channel.shutdownNow();
  }

  /**
   * Blocking call to report
   */
  public ReportResponse report(ReportRequest request) {
    return blockingStub.report(request);
  }

}
