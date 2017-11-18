commit 83b6d323481ae00b194342953725e64ff483d34e
Author: Benjamin Bentmann <benjamin.bentmann@udo.edu>
Date:   Wed Aug 10 16:57:52 2011 +0200

    [AHC-107] Active uploads that take longer than configured request timeout get cancelled

    o Touched future upon operation progress which empirically improves the situation but whether this solves the issue entirely is beyond my current understanding of the internals