commit a99e3536e4429a60f370532faec6236fd257d312
Author: Gerrit Uitslag <klapinklapin@gmail.com>
Date:   Thu Apr 28 00:17:33 2016 +0200

    RemoteAPICore improvements and unittests

    RemoteAPICore
    - visibility methods
    - getAttachmentInfo returns modification date of deleted files
    - pageVersions returns only current revision (once) if $first=0.
    Adresses issue mentioned at #1550
    - added unittests
    - updated API number