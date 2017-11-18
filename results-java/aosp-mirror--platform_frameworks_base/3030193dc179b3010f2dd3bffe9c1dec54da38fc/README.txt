commit 3030193dc179b3010f2dd3bffe9c1dec54da38fc
Author: Gilles Debunne <debunne@google.com>
Date:   Wed Jun 16 18:32:00 2010 -0700

    Removed warnings in LayoutInflater.

    These changes are similar to those of CL 49296. They do not include the
    generic fixes done on GenericInflater.java, which had issues and broke the build.

    Added a asSubClass method in LayoutInflater which will (correctly) throw a
    ClassCastException when the inflated class is not a View subclass.

    Performance testing on these changes showed a 10% performance improvement,
    which is still to be explained.

    Change-Id: Id4d3b45f0945baccdbbda15fcce095e855b23c9a