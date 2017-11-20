commit 08fb0ff708144cf8d9f09476d89d86dcd998f37a
Author: Craig H <craigham@users.noreply.github.com>
Date:   Fri Jul 15 20:12:33 2016 -0700

    refactor to reflect ability to connect to plm via tcp (#4521)

    * renamed OldHubIOStream to TcpIOStream to reflect its usability with
    programs such as ser2net.

    refactored factory method in IOStream to allow tcp as a prefix within
    config

    * add javadoc to factory method, and also renamed method which creates the TcpIOStream to reflect its more generic usage