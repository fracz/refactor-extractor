commit e7678517b3f77fbd4b036f643ad4f22989068483
Author: Hugo Benichi <hugobenichi@google.com>
Date:   Tue May 9 15:19:01 2017 +0900

    ConnectivityManager: improve argument validation

    Using Preconditions and dedicated static methods for checking arguments
    to improve error stack traces without error messages.

    Test: covered by previously added unit test
    Bug: 36701874
    Change-Id: Id872b2c887a4bca43a8c3644622add1c2ee57c6d