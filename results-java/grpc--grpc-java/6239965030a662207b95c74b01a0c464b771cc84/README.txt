commit 6239965030a662207b95c74b01a0c464b771cc84
Author: Eric Anderson <ejona@google.com>
Date:   Thu Jan 15 18:10:11 2015 -0800

    Improve test client for real cert

    The test client can now be used against a server that has properly
    configured certificates, instead of just the test server. To reach the
    test server, the client needs these arguments:
    --use_test_ca=true --server_host_override=foo.test.google.fr

    Client no longer has any required arguments, although for any given
    setup needing to specify at least one argument is highly likely.

    The arguments have been improved in general to hopefully be more
    orthogonal and match those of other language's test clients.