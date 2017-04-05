commit 118f33aedad84e9f8a01e4db983545d18fdd1fc3
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Wed Mar 29 11:51:51 2017 -0400

    Request body improvements in WebClient, WebTestClient

    This commit makes changes to WebClient and WebTestClient in oder to
    limit setting the body according to HTTP method and also to facilitate
    providing the request body as Object.

    Specifically, this commit:

     - Moves methods that operate on the request body to a RequestBodySpec
     in both WebClient and WebTestClient, and rename them to `body`.
     These methods now just *set* the body, without performing
     an exchange (which now requires an explicit exchange call).
     - Parameterizes UriSpec in both WebClient and WebTestClient, so that
     it returns either a RequestHeadersSpec or a RequestBodySpec.

    Issue: SPR-15394