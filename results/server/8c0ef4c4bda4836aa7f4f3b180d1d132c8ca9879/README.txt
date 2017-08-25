commit 8c0ef4c4bda4836aa7f4f3b180d1d132c8ca9879
Author: Roeland Jago Douma <rullzer@owncloud.com>
Date:   Thu Mar 3 20:58:18 2016 +0100

    Add sharePermissions webdav property

    This property can be queries by the clients so they know the max
    permissions they can use to share a file with. This will improve the UX.

    The oc:permissions proptery is not enough since mountpoints have
    different permissions (delete + move by default).

    By making it a new property the clients can just request it. On older
    servers it will just return a 404 for that property (and thus they know
    they have to fall back to their hacky work arounds). But if the property
    is returned the client can show proper info.

    * unit tests
    * intergration test