commit 69b8b29c7b1be1454ba9b914b4f7697d03f9bc56
Author: Tobias Thierer <tobiast@google.com>
Date:   Tue Apr 11 22:16:53 2017 +0100

    Network: Use HttpURLConnectionFactory rather than OkHttp APIs

    This is a refactoring with no behavior change.

    The new class provides an abstraction layer to hide knowledge
    about OkHttp-specific APIs. Logic from android.net.Network that
    knew about OkHttp is moving into that abstraction layer.

    This CL refactors android.net.Network to make use of this
    abstraction layer instead of the tight coupling onto OkHttp
    APIs. The class no longer imports any classes from okhttp
    packages.

    The values of mDns and mConnectionPool, which never change after
    the initial call to maybeInitHttpClient(), are now set directly on
    the AndroidHttpClient instance when it is constructed in that method.

    Applications can overwrite getSocketFactory() and might depend on
    that method being called (and the result used) every time a
    connection is opened; therefore, for maximum app compatibility this
    call was kept inside openConnection().

    This CL is a prerequisite for introducing an additional frameworks
    dependency on a richer API than HttpURLConnection.

    Test: Build and install apk for FrameworksCoreTests, then run:
          adb shell am instrument -e class android.net.NetworkTest -w com.android.frameworks.coretests
    Bug: 64021405

    Change-Id: I2c73d260508ee20c6a40fd6e95e2d058d3ea2330