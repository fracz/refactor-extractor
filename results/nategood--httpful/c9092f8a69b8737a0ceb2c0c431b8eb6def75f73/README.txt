commit c9092f8a69b8737a0ceb2c0c431b8eb6def75f73
Author: nickl- <nick@jigsoft.co.za>
Date:   Sun Jun 17 09:48:31 2012 +0200

    Compose request raw_header.

    Mainly done to be able to test against the composed header without a bajor refactor.
    This is now similar to the response and although there are other ways to get this information from cURL this now makes HttpFul complete being able to provide both request and response headers.

    Note the HTTP version is hard coded this might require additional work but should be efficient for the majority of use cases.