commit b3788d8dcbeee7a20f562e878c187a75bac11ff0
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Tue May 30 12:53:32 2017 -0700

    KAFKA-5316; Follow-up with ByteBufferOutputStream and other misc improvements

    ByteBufferOutputStream improvements:
    * Document pitfalls
    * Improve efficiency when dealing with direct byte buffers
    * Improve handling of buffer expansion
    * Be consistent about using `limit` instead of `capacity`
    * Add constructors that allocate the internal buffer

    Other minor changes:
    * Fix log warning to specify correct Kafka version
    * Clean-ups

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jason Gustafson <jason@confluent.io>

    Closes #3166 from ijuma/minor-kafka-5316-follow-ups