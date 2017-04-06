commit 69fb23516035a223ab5cd674ebdc281b4bb5c2a3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 24 21:36:12 2011 +0100

    Revert "[DoctrineBundle] Fix bug in Auto Proxy Generation introduced with config merge refactoring. Use array_reverse() for $configs to fix incremental merge algorithm assumptions."

    This reverts commit 1bc0b54411d6fe35ad816b032ae2c3eb22d7d732.