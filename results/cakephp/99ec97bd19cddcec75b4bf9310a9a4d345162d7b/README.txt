commit 99ec97bd19cddcec75b4bf9310a9a4d345162d7b
Author: Schlaefer <openmail+sourcecode@siezi.com>
Date:   Fri Sep 27 16:39:23 2013 +0200

    performance improvements in Model::_clearCache

    - don't empty cache twice if pluralized name is identical to underscored
    - refactors for fewer function calls