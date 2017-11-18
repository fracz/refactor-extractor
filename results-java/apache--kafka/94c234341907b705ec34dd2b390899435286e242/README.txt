commit 94c234341907b705ec34dd2b390899435286e242
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Mon Mar 7 11:34:12 2016 -0800

    KAFKA-3341: Improve error handling on invalid requests

    * Include request id when parsing of request header fails
    * Don't mute selector on a connection that was closed due to an error (otherwise a second exception is thrown)
    * Throw appropriate exception from `ApiKeys.fromId` if invalid id is passed
    * Fail fast in `AbstractRequest.getRequest` if we fail to handle an instance of `ApiKeys` (if this happens, it's a programmer error and the code in `getRequest` needs to be updated)

    I ran into the top two issues while trying to figure out why a connection from a producer to a broker was failing (and it made things harder than necessary). While fixing them, I noticed the third and fourth issues.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Gwen Shapira

    Closes #1017 from ijuma/kafka-3341-improve-error-handling-invalid-requests