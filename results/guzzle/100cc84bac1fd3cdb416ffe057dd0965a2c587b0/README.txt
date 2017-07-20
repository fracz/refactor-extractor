commit 100cc84bac1fd3cdb416ffe057dd0965a2c587b0
Author: fogs <fogs@example.com>
Date:   Fri Nov 9 12:03:43 2012 +0100

    Enhanced validity checking for cookies

    This patch refactors the validity checking of a cookie from ArrayCookieJar::add() to a dedicated method in Cookie, where it should better belong to. Also, the check for a valid cookie name now includes RFC compliance with regards to control characters.

    PHPUnitTest cases were extended accordingly.