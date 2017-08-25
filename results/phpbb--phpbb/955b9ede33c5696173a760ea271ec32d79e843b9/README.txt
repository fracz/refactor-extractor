commit 955b9ede33c5696173a760ea271ec32d79e843b9
Author: Mate Bartus <mate.bartus@gmail.com>
Date:   Thu Feb 11 13:18:30 2016 +0100

    [ticket/14462] Further speed improvements

    - Cache the secondary container
    - Only initialize tasks/modules that are being used
    - Add timeout error message in the AJAX UI

    PHPBB3-14462