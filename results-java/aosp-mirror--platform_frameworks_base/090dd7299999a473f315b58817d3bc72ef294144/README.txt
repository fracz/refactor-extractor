commit 090dd7299999a473f315b58817d3bc72ef294144
Author: Jack Yu <jackyu@google.com>
Date:   Fri Dec 18 15:16:24 2015 -0800

    Data call retry refactoring

    Moved retry manager from DataConnection to ApnContext. Moved the
    configuration from system properties to carrier config.

    bug: 22208242
    Change-Id: I224d4b0fd0115b964e85a07e2c1b5061726ca91b