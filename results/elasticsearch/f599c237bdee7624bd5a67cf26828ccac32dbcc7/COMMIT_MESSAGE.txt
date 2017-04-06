commit f599c237bdee7624bd5a67cf26828ccac32dbcc7
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Apr 27 20:29:57 2015 -0400

    Security manager cleanups

    1. initialize SM after things like mlockall. Their tests currently
       don't run with securitymanager enabled, and its simpler to just
       run mlockall etc first.
    2. remove redundant test permissions (junit4.childvm.cwd/temp). This
       is alreay added as java.io.tmpdir.
    3. improve tests to load the generated policy with some various
       settings and assert things about the permissions on configured
       directories.
    4. refactor logic to make it easier to fine-grain the permissions later.
       for example we currently allow write access to conf/. In the future
       I think we can improve testing so we are able to make improvements here.