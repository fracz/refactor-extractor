commit 618535f576d0ee69d1b5d65561a33b65fc5cb1e6
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Mon Jun 27 12:33:21 2016 +0100

    Polish “Allow management server SSL to be configured independently”

    This commit polishes b0fbc7e, throwing an exception when an attempt is
    made to configure management-specific SSL without also configuring a
    custom management port. The testing of management-specific SSL
    configuration has also been improved.

    See gh-6057
    Closes gh-4810