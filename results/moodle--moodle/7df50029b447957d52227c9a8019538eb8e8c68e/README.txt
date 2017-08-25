commit 7df50029b447957d52227c9a8019538eb8e8c68e
Author: Petr Skoda <commits@skodak.org>
Date:   Sat Apr 9 10:27:51 2011 +0200

    MDL-25826 integrate HTMLPurifier 4.3.0 and improve performance

    The new HTMLPurifier finally caches the schema properly eliminating both extra CPU cycles and disk writes. The repeated dir exists tests might cause problems on NFS shares.