commit 15e5247e03bbf7b69ac547c2bad3d17a89149c61
Author: Robert Muir <rmuir@apache.org>
Date:   Wed Aug 5 20:24:36 2015 -0400

    Get plugin smoketester running in jenkins.

    We have a smoke_test_plugins.py, but its a bit slow, not integrated
    into our build, etc.

    I converted this into an integration test. It is definitely uglier
    but more robust and fast (e.g. 20 seconds time to verify).

    Also there is refactoring of existing integ tests logic, like printing
    out commands we execute and stuff