commit c4d629a0b3cbd11c174cb8b09a50bc8de77825e9
Author: Jason Gustafson <jason@confluent.io>
Date:   Fri Aug 25 10:23:11 2017 -0700

    MINOR: Consolidate broker request/response handling

    This patch contains a few small improvements to make request/response handling more consistent. Primarily it consolidates request/response serialization logic so that `SaslServerAuthenticator` and `KafkaApis` follow the same path. It also reduces the amount of custom logic needed to handle unsupported versions of the ApiVersions requests.

    Author: Jason Gustafson <jason@confluent.io>

    Reviewers: Ismael Juma <ismael@juma.me.uk>

    Closes #3673 from hachikuji/consolidate-response-handling