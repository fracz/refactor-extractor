commit 9b09280470204da3a77483a2fbcab2da58080975
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Fri Sep 25 17:14:19 2015 +0200

    Allow upgrade from older store formats without id files

    Store backup does not always copy id files (e.g., in 2.2.5), hence in
    order to allow migration from a backup we cannot rely on having id
    files.  This change will add test a small refactor to allow migration
    without ids when possible, i.e., for stores from 2.1.x and 2.2.x