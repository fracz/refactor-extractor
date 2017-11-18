commit a5c47db1382c14720106ae1da20d2b332f89c22c
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Sat Jun 24 08:20:44 2017 +0100

    KAFKA-5506; Fix NPE in OffsetFetchRequest.toString and logging improvements

    NetworkClient's logging improvements:
    - Include correlation id in a number of log statements
    - Avoid eager toString call in parameter passed to log.debug
    - Use node.toString instead of passing a subset of fields to the
    logger
    - Use requestBuilder instead of clientRequest in one of the log
    statements

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Damian Guy <damian.guy@gmail.com>, Jason Gustafson <jason@confluent.io>

    Closes #3420 from ijuma/kafka-5506-offset-fetch-request-to-string-npe