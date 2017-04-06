commit f5ff69fabe68cb2e2fa941f7d4307cd730073709
Author: Simon Willnauer <simon.willnauer@elasticsearch.com>
Date:   Tue Nov 29 09:35:07 2016 +0100

    Remove connectToNodeLight and replace it with a connection profile (#21799)

    The Transport#connectToNodeLight concepts is confusing and not very flexible.
    neither really testable on a unittest level. This commit cleans up the code used
    to connect to nodes and simplifies transport implementations to share more code.
    This also allows to connect to nodes with custom profiles if needed, for instance
    future improvements can be added to connect to/from nodes that are non-data nodes without
    dedicated bulks and recovery connections.