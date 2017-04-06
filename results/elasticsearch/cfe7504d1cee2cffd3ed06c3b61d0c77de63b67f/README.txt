commit cfe7504d1cee2cffd3ed06c3b61d0c77de63b67f
Author: Shay Banon <kimchy@gmail.com>
Date:   Wed Sep 26 23:46:27 2012 +0200

    introduce TransportRequest (with optional headers)
    introduce a new class, TransportRequest, which includes headers. This class can be used when sending requests over the transport layer, and ActionRequest also extends it now.
    This is the first phase of the refactoring part in the transport layer and action layer to allow for simpler implementations of those as well as simpler "filtering" capabilities in the future