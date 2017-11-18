commit ef3f2620b3a755856d70345fc7a90df896985c26
Author: Ben Kwa <kenobi@google.com>
Date:   Tue Apr 7 15:43:39 2015 -0700

    Prototype the destination picking.

    - Add an intent to open a destination picker, and refactor
    DocumentsActivity accordingly.
    - Modify CopyService to take a destination for the copy, and to use URIs
    and PFDs instead of Files and Streams, for better error handling &
    cleanup.

    Change-Id: I69dc43823a703674dc29d2215e2df23b33ad7882