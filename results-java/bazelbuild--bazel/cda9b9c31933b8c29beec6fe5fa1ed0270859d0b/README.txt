commit cda9b9c31933b8c29beec6fe5fa1ed0270859d0b
Author: ulfjack <ulfjack@google.com>
Date:   Fri Jun 9 09:49:07 2017 -0400

    Remote execution: post CAS links to the BEP instead of local file URIs

    Also use DigestUtils for performance, which has a static cache of the digests.
    This isn't ideal, but necessary for now to avoid computing the digests multiple
    times. We'll need to clean this up afterwards by including the digests in the
    posted events, and making them available to the PathConverter. I have some
    changes towards that (as well as some submitted already), but it requires
    quite some refactoring.

    RELNOTES: Bazel posts links to the CAS to the BEP if remote caching / execution is enabled
    PiperOrigin-RevId: 158513165