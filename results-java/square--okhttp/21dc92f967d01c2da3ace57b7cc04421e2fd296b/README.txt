commit 21dc92f967d01c2da3ace57b7cc04421e2fd296b
Author: jwilson <jwilson@squareup.com>
Date:   Sun Aug 11 10:18:34 2013 -0400

    Support multiple variants of the SPDY protocol.

    This behavior-free refactoring makes the first baby steps towards
    supporting HTTP/2.0. It adds indirection on the framing layer so
    we can frame either using SPDY/3's syntax or HTTP/2.0's.