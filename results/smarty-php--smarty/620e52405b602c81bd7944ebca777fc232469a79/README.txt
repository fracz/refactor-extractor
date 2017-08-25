commit 620e52405b602c81bd7944ebca777fc232469a79
Author: uwetews <uwe.tews@googlemail.com>
Date:   Tue Sep 1 01:54:28 2015 +0200

    - improvement convert template inheritance into runtime processing
      - bugfix {$smarty.block.parent} did always reference the root parent block https://github.com/smarty-php/smarty/issues/68