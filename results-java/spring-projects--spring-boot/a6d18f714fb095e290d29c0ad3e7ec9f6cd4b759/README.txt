commit a6d18f714fb095e290d29c0ad3e7ec9f6cd4b759
Author: Vedran Pavic <vedran.pavic@gmail.com>
Date:   Sun Nov 20 17:04:17 2016 +0100

    Add JMX without backing `Endpoint` support

    Decompose `EndpointMBean` to improve support for JMX endpoints without
    a backing `Endpoint`.

    See gh-6579