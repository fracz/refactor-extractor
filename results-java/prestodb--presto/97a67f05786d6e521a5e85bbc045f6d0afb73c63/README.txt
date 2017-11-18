commit 97a67f05786d6e521a5e85bbc045f6d0afb73c63
Author: Christopher Berner <christopherberner@gmail.com>
Date:   Wed Jan 20 19:51:47 2016 -0800

    Fix IndexOutOfBoundsException in TopologyAwareScheduler

    This correctly handles the case when the server is first starting up and
    the NetworkLocationCache is empty. Also, improve the test coverage for
    the NetworkLocationCache.