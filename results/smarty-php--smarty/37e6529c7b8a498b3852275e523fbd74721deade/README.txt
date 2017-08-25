commit 37e6529c7b8a498b3852275e523fbd74721deade
Author: uwetews <uwe.tews@googlemail.com>
Date:   Tue Sep 1 02:10:15 2015 +0200

    - improvement convert template inheritance into runtime processing
      - bugfix {$smarty.block.parent} did always reference the root parent block https://github.com/smarty-php/smarty/issues/68