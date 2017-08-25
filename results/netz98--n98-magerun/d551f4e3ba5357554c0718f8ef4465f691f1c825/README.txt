commit d551f4e3ba5357554c0718f8ef4465f691f1c825
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Wed Jul 15 22:56:34 2015 +0200

    Fixed Cookie Domain Check

    Put the cookie-domain-check under unit-testing and found some flaws by
    that.

    Fixed the cookie domain check for the most common configuration for stable
    use.

    This still does not check against all nuances which could bite oneself
    but coverage is much higher now. Also it's easier to test in case of a
    regression.

    Additionally the error message wording has been improved.