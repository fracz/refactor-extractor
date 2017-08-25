commit beae69440167d7aa1c4660cc9134f2c863df252f
Author: Sean Rand <asanernd@gmail.com>
Date:   Fri May 3 13:29:27 2013 +0200

    Add OPML exporting and adapt OPML controller

    This rewrites large parts of the OPML controller and adds exporting
    of all subscriptions as OPML files via http://selfoss/opmlexport.

    The generated OPML files should be compliant with the OPML 2.0 specs
    (http://dev.opml.org/spec2.html). The subscription list is written as
    a tree, with each subscription being a child of each of its tags.

    Attributes and elements that aren't part of the OPML 2.0 specs are
    saved in a seperate namespace. These are: the tag colors, spout names,
    spout parameters and other meta information.

    OPML importing has been refactored and adapted to read the information
    from the selfoss namespace and to import tag colors and spouts.