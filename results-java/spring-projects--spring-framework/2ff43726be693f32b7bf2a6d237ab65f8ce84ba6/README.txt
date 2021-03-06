commit 2ff43726be693f32b7bf2a6d237ab65f8ce84ba6
Author: Chris Beams <cbeams@vmware.com>
Date:   Mon May 14 14:47:11 2012 +0300

    Restore serializability of HttpStatusCodeException

    SPR-7591 introduced a java.nio.charset.Charset field within
    HttpStatusCodeException. The former is non-serializable, thus by
    extension the latter also became non-serializable.

    Because the Charset field is only used for outputting the charset name
    in HttpStatusCodeException#getResponseBodyAsString, it is reasonable to
    store the value returned by Charset#name() instead of the actual Charset
    object itself.

    This commit refactors HttpStatusCodeException's responseCharset field to
    be of type String instead of Charset and adds tests to prove that
    HttpStatusCodeException objects are once again serializable as expected.

    Issue: SPR-9273, SPR-7591