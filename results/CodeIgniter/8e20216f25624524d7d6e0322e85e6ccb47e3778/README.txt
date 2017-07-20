commit 8e20216f25624524d7d6e0322e85e6ccb47e3778
Author: Andrey Andreev <narf@devilix.net>
Date:   Wed Feb 5 18:59:55 2014 +0200

    More CI_Encryption improvements

     - Make OpenSSL the default driver if available (because MCrypt is stupid).
     - Require MCRYPT_DEV_URANDOM for the MCrypt availability check
       (because security; also, incidentally - it's faster that way ;)).