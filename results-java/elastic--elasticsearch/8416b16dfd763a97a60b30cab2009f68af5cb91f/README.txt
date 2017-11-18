commit 8416b16dfd763a97a60b30cab2009f68af5cb91f
Author: Jason Tedor <jason@tedor.me>
Date:   Wed Nov 23 15:49:05 2016 -0500

    Improve handling of unreleased versions

    Today when handling unreleased versions for backwards compatilibity
    support, we scatted version constants across the code base and add some
    asserts to support removing these constants when the version in question
    is actually released. This commit improves this situation, enabling us
    to just add a single unreleased version constant that can be renamed
    when the version is actually released. This should make maintenance of
    these versions simpler.

    Relates #21760