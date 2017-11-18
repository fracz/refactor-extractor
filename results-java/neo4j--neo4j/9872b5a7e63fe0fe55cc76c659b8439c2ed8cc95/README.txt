commit 9872b5a7e63fe0fe55cc76c659b8439c2ed8cc95
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Jun 9 11:25:23 2016 +0200

    Remove last applying storage

    On startup read the last applying index from the state machines rather
    than writing it in a storage file on disk.

    This should improve performance when applying commands since there is
    no need to flush on disk applying index updates.