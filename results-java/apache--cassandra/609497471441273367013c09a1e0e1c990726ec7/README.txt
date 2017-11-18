commit 609497471441273367013c09a1e0e1c990726ec7
Author: Benedict Elliott Smith <benedict@apache.org>
Date:   Thu Jul 30 13:31:42 2015 +0100

    9975: Flatten Iterator Transformation Hierarchy

    To improve clarity of control flow, all iterator transformations
    are applied via a single class that manages an explicit stack of
    named transformation objects.

    patch by benedict; reviewed by branimir for CASSANDRA-9975