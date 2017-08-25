commit 8f34cf3d32c9c091caa658472bd4e3a8270969a8
Author: Michael Große <grosse@cosmocode.de>
Date:   Wed Jul 26 16:35:49 2017 +0200

    Fix PHP Notices: Reduce error log noise

    While DokuWiki suppresses PHP Notices they are still a code smell and
    should be fixed. This fixes some PHP Notices that occurred.

    Some of these fixes could be refactored into nicer code once we move to
    PHP 7 and get access to the `??` operator.