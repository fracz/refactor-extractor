commit fd0ae840b64120efd2a29ba96f0467fda0f21dad
Author: Norman Maurer <norman_maurer@apple.com>
Date:   Thu Dec 1 08:13:33 2016 +0100

    Small performance improvements in ResourceLeakDetector

    Motivation:

    42fba015ce82ab4ab30e547c888db82fe74094e9 changed the implemention of ResourceLeakDetector to improve performance. While this was done a branch was missed that can be removed. Beside this  using a Boolean as value for the ConcurrentMap is sub-optimal as when calling remove(key, value) an uncessary instanceof check and cast is needed on each removal.

    Modifications:

    - Remove branch which is not needed anymore
    - Replace usage of Boolean as value type of the ConcurrentMap and use our own special type which only compute hash-code one time and use a == operation for equals(...) to reduce overhead present when using Boolean.

    Result:

    Faster and cleaner ResourceLeakDetector.