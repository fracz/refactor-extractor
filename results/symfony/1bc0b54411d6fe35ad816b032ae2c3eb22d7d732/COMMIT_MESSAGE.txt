commit 1bc0b54411d6fe35ad816b032ae2c3eb22d7d732
Author: beberlei <kontakt@beberlei.de>
Date:   Mon Jan 24 10:21:46 2011 +0100

    [DoctrineBundle] Fix bug in Auto Proxy Generation introduced with config merge refactoring. Use array_reverse() for $configs to fix incremental merge algorithm assumptions.