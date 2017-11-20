package com.google.net.stubby.newtransport;

import com.google.net.stubby.transport.Transport;

import java.net.HttpURLConnection;

/**
 * Constants for GRPC-over-HTTP (or HTTP/2)
 */
public final class HttpUtil {
  /**
   * The Content-Type header name. Defined here since it is not explicitly defined by the HTTP/2
   * spec.
   */
  public static final String CONTENT_TYPE_HEADER = "content-type";

  /**
   * The Content-Length header name. Defined here since it is not explicitly defined by the HTTP/2
   * spec.
   */
  public static final String CONTENT_LENGTH_HEADER = "content-length";

  /**
   * Content-Type used for GRPC-over-HTTP/2.
   */
  public static final String CONTENT_TYPE_PROTORPC = "application/protorpc";

  /**
   * The HTTP method used for GRPC requests.
   */
  public static final String HTTP_METHOD = "POST";

  /**
   * The Header name for the gRPC status found in the trailers.
   */
  public static final String GRPC_STATUS_HEADER = "grpc-status";

  /**
   * Maps HTTP error response status codes to transport codes.
   */
  public static Transport.Code httpStatusToTransportCode(int httpStatusCode) {
    // Specific HTTP code handling.
    switch (httpStatusCode) {
      case HttpURLConnection.HTTP_UNAUTHORIZED: // 401
        return Transport.Code.UNAUTHENTICATED;
      case HttpURLConnection.HTTP_FORBIDDEN: // 403
        return Transport.Code.PERMISSION_DENIED;
      default:
    }
    // Generic HTTP code handling.
    if (httpStatusCode < 300) {
      return Transport.Code.OK;
    }
    if (httpStatusCode < 400) {
      return Transport.Code.UNAVAILABLE;
    }
    if (httpStatusCode < 500) {
      return Transport.Code.INVALID_ARGUMENT;
    }
    if (httpStatusCode < 600) {
      return Transport.Code.FAILED_PRECONDITION;
    }
    return Transport.Code.INTERNAL;
  }

  private HttpUtil() {}
}