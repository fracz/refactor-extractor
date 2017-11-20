commit ff0ad5fac3cf944a5ce5fb3ccef7df4a2d0bdf14
Author: ZHANG Dapeng <zdapeng@google.com>
Date:   Mon Jul 10 16:30:38 2017 -0700

    testing: refactor part of TestUtils to internal

    Moved the following APIs from `io.grpc.testing.TestUtils` to `io.grpc.internal.TestUtils`:

    `InetSocketAddress testServerAddress(String host, int port)`
    `InetSocketAddress testServerAddress(int port)`
    `List<String> preferredTestCiphers()`
    `File loadCert(String name)`
    `X509Certificate loadX509Cert(String fileName)`
    `SSLSocketFactory newSslSocketFactoryForCa(Provider provider, File certChainFile)`
    `void sleepAtLeast(long millis)`

    APIs not to be moved:

    `ServerInterceptor recordRequestHeadersInterceptor()`
    `ServerInterceptor recordServerCallInterceptor()`