commit 150858b40e98ee969b74a10702de06f974b757be
Author: janakr <janakr@google.com>
Date:   Tue Jul 25 00:04:53 2017 +0200

    Add hook to process SkyframeExecutor in tests. Also add sentinel exception to indicate unserializability, improve error message in Path when serialization fails, and add some test-only methods to SkyframeExecutor and PackageFactory.

    PiperOrigin-RevId: 162993806