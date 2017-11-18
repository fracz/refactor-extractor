commit ce4b4da5838dbb343c216f51af05ce3e75b1a39a
Author: Scott Mitchell <scott_mitchell@apple.com>
Date:   Thu Aug 10 16:48:38 2017 -0700

    SocketStringEchoTest improvements

    Motivation:
    SocketStringEchoTest has been observed to fail and it has numerous issues which when resolved may help reduce the test failures.

    Modifications:
    - A volatile counter and a spin/sleep loop is used to trigger test termination. Incrementing a volatile is generally bad practice and can be avoided in this situation. This mechanism can be replaced by a promise. This mechanism should also trigger upon exception or channel inactive.
    - Asserts are done in the Netty threads. Although these should result in a exceptionCaught the test may not observe these failures because it is spinning waiting for the count to reach the desired value.

    Result:
    Cleaner test.