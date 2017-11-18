commit d8a63817d8c5f7615c0fbe45b0245f64520f4eea
Author: Chris Vest <mr.chrisvest@gmail.com>
Date:   Wed Nov 19 10:14:30 2014 +0100

    Don't use reflection for creating the transpot DirectByteBuffers

    I tried using MethodHandles, but the security features of Java foiled that attempt.
    So now I use Unsafe to simulate the construction.
    The reflection based approach is kept around as a fallback, in case the java.nio internals are refactored in a future version of Java.