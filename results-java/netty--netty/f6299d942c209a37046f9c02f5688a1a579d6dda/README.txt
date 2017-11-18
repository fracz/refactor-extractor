commit f6299d942c209a37046f9c02f5688a1a579d6dda
Author: Stephane Landelle <slandelle@gatling.io>
Date:   Mon May 11 13:11:39 2015 +0200

    Minor ClientCookieDecoder improvements

    Motivation:

    * Path attribute should be null, not empty String, if it's passed as "Path=".
    * Only extract attribute value when the name is recognized.
    * Only extract Expires attribute value String if MaxAge is undefined as it has precedence.

    Modification:

    Modify ClientCookieDecoder.
    Add "testIgnoreEmptyPath" test in ClientCookieDecoderTest.

    Result:

    More idyomatic Path behavior (like Domain).
    Minor performance improvement in some corner cases.