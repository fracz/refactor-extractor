commit 4211155eb223fdd3bc67534377757453ba2de398
Author: Steve Clay <steve@mrclay.org>
Date:   Sun Feb 21 13:44:41 2016 -0500

    feature(ajax): improves the elgg/Ajax API and adds docs

    Modifies the response hooks API so that handlers receive a wrapper object
    rather than the simple return value. This allows handlers to more reliably
    attach metadata to the response without the need to "wrap" the value.

    Requests can specify that system messages be left in the queue. This is
    sometimes desirable if the client code intends to redirect/reload a page.

    Prefixes with `elgg_` the server-side options.

    The client-side response hook no longer fires twice.

    Moves the system message delivery to hooks.

    Documents using hooks to filter requests and responses.

    Fixes #9404