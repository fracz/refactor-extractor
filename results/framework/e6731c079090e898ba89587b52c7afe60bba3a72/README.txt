commit e6731c079090e898ba89587b52c7afe60bba3a72
Author: Sébastien Nikolaou <info@sebdesign.eu>
Date:   Wed Jan 13 03:37:15 2016 +0200

    Refactor MocksApplicationServices

    Add `doesntExpectEvents` method and refactor trait to share code with
    `expectsEvents` method.

    Addresses #11386.