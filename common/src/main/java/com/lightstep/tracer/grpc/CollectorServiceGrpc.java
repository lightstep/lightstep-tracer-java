package com.lightstep.tracer.grpc;

import io.grpc.CallOptions;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.1.1)",
    comments = "Source: collector.proto")
public class CollectorServiceGrpc {

  private CollectorServiceGrpc() {}

  public static final String SERVICE_NAME = "lightstep.collector.CollectorService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.lightstep.tracer.grpc.ReportRequest,
      com.lightstep.tracer.grpc.ReportResponse> METHOD_REPORT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "lightstep.collector.CollectorService", "Report"),
          io.grpc.protobuf.ProtoUtils.marshaller(com.lightstep.tracer.grpc.ReportRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(com.lightstep.tracer.grpc.ReportResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CollectorServiceStub newStub(io.grpc.Channel channel) {
    return new CollectorServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CollectorServiceBlockingStub newBlockingStub(
          io.grpc.Channel channel, CallOptions callOptions) {
    return new CollectorServiceBlockingStub(channel, callOptions);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static CollectorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CollectorServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class CollectorServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void report(com.lightstep.tracer.grpc.ReportRequest request,
        io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.ReportResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REPORT, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_REPORT,
            asyncUnaryCall(
              new MethodHandlers<
                com.lightstep.tracer.grpc.ReportRequest,
                com.lightstep.tracer.grpc.ReportResponse>(
                  this, METHODID_REPORT)))
          .build();
    }
  }

  /**
   */
  public static final class CollectorServiceStub extends io.grpc.stub.AbstractStub<CollectorServiceStub> {
    private CollectorServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CollectorServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CollectorServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CollectorServiceStub(channel, callOptions);
    }

    /**
     */
    public void report(com.lightstep.tracer.grpc.ReportRequest request,
        io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.ReportResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPORT, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CollectorServiceBlockingStub extends io.grpc.stub.AbstractStub<CollectorServiceBlockingStub> {
    private CollectorServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CollectorServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CollectorServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CollectorServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.lightstep.tracer.grpc.ReportResponse report(com.lightstep.tracer.grpc.ReportRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPORT, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CollectorServiceFutureStub extends io.grpc.stub.AbstractStub<CollectorServiceFutureStub> {
    private CollectorServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CollectorServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CollectorServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CollectorServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.lightstep.tracer.grpc.ReportResponse> report(
        com.lightstep.tracer.grpc.ReportRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPORT, getCallOptions()), request);
    }
  }

  private static final int METHODID_REPORT = 0;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CollectorServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(CollectorServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REPORT:
          serviceImpl.report((com.lightstep.tracer.grpc.ReportRequest) request,
              (io.grpc.stub.StreamObserver<com.lightstep.tracer.grpc.ReportResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class CollectorServiceDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.lightstep.tracer.grpc.Collector.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CollectorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CollectorServiceDescriptorSupplier())
              .addMethod(METHOD_REPORT)
              .build();
        }
      }
    }
    return result;
  }
}
