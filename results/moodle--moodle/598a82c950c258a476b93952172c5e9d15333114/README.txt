commit 598a82c950c258a476b93952172c5e9d15333114
Author: Petr Škoda <commits@skodak.org>
Date:   Sun Aug 4 00:01:58 2013 +0200

    MDL-41019 improve language caching

    Includes:
    * no more hacky reloads, everything is written only once and kept until cache reset
    * lang menu list is now cached in MUC
    * both string and lang menu caches are compatible with local caches on cluster nodes
    * config-dist.php cleanup