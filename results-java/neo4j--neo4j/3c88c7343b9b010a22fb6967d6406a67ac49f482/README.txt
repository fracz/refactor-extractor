commit 3c88c7343b9b010a22fb6967d6406a67ac49f482
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Oct 20 14:20:59 2016 +0200

    Better timeout on core startup

    Giving only 1 minute to download a snapshot is not enough in many
    cases. E.g., we have seen several failure of tests in CI.  This change
    will improve the time out by waiting longer (30 minutes). Moreover
    every 30 seconds a warning is logged explaining that the shapshot
    download is not completed yet and we have to keep waiting longer.