commit 100176bf520a7605b0151321038c1a0e6a354a23
Author: Steve Clay <steve@mrclay.org>
Date:   Thu Dec 18 09:44:08 2014 -0500

    chore(views): refactor system cache and views for more info capture

    This moves the logic of sniffing plugin dirs for views into the ViewsService
    so it’s less coupled to ElggPlugin. Also we add tracking of views whose
    locations have been overridden. We save them in the system cache but don’t
    need to load them for requests; that can be done by the views inspector if
    it needs them.