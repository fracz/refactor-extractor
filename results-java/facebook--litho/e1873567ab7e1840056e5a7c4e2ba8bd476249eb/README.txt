commit e1873567ab7e1840056e5a7c4e2ba8bd476249eb
Author: Rick Brewster <rickbrew@fb.com>
Date:   Tue Jun 27 11:43:09 2017 -0700

    Refactor ResourceCache into abstract base class and derived LruResourceCache class

    Summary:
    In preparation for experimenting with the implementation of ResourceCache,
    it is useful to refactor it so we can actually have different implementations.

    Reviewed By: pasqualeanatriello

    Differential Revision: D5323949

    fbshipit-source-id: 8d0053e15787e6d6aea27fc439e5b8ad77981512