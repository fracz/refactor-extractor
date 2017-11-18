commit 8265a43897b3da88839df061ce22559820087ad9
Author: Jason Gustafson <jason@confluent.io>
Date:   Mon Aug 14 14:05:40 2017 +0100

    MINOR: Safer handling of requests prior to SASL authentication

    This implements two improvements for request handling prior to SASL authentication:

    1. Only parse request types that are allowed prior to authentication.
    2. Limit the maximum request size (the default is 100Mb).

    Author: Jason Gustafson <jason@confluent.io>

    Reviewers: Rajini Sivaram <rajinisivaram@googlemail.com>, Ismael Juma <ismael@juma.me.uk>

    Closes #3558 from hachikuji/minor-restrict-presasl-request-parsing