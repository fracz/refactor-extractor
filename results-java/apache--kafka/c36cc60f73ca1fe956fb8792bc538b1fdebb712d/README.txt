commit c36cc60f73ca1fe956fb8792bc538b1fdebb712d
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Wed May 18 11:01:27 2016 -0700

    MINOR: Use `Record` instead of `ByteBufferMessageSet` in `ProduceRequestTest`

    We want to phase out `ByteBufferMessageSet` eventually, so new code should favour `Record` where possible.

    Also use a fixed timestamp in `testCorruptLz4ProduceRequest` to ensure that
    the checksum is always the same.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jason Gustafson, Guozhang Wang

    Closes #1357 from ijuma/produce-request-test-improvement