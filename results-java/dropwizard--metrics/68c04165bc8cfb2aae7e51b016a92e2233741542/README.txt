commit 68c04165bc8cfb2aae7e51b016a92e2233741542
Author: Artem Prigoda <arteamon@gmail.com>
Date:   Fri Jun 2 10:48:32 2017 +0200

    Timer.Context now implements AutoCloseable

    Small improvement that allows Timer.Context to be used in try-with-resources

    (cherry picked from commit 02496541dcbfa33fe7051c081bf5f359187a1f27)

    # Conflicts:
    #       metrics-adapter/src/main/java/com/codahale/metrics/Timer.java