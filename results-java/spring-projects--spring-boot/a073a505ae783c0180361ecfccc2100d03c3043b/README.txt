commit a073a505ae783c0180361ecfccc2100d03c3043b
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Wed Jul 8 18:26:25 2015 +0200

    Move spring.oauth2.* to security.oauth2.*

    Unfortunately, we have no other choice to flip the ignoreUnknownFields
    attribute of `SecurityProperties` has many different target are now set
    for that namespace outside the class. See gh-3445 for a potential way
    to improve that.

    Closes gh-3327