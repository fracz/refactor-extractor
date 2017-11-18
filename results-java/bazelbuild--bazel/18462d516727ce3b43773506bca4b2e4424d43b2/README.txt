commit 18462d516727ce3b43773506bca4b2e4424d43b2
Author: Googler <noreply@google.com>
Date:   Tue Oct 31 12:54:26 2017 -0400

    Rename ResourceFilter to ResourceFilterFactory

    In the next review, to handle issues around density filtering, ResourceFilterFactory will return another object that actually handles filtering. To ensure stuff is named properly, rename ResourceFilter to ResourceFilterFactory now so that the new class can be called ResourceFilter.

    This is a straightforward automated refactor, followed with some automated reformatting to make linting happy.

    I used the name ResourceFilterFactory, rather than the more concise ResourceFilters, as this class actually contains state (both around what filtering should currently do and about what resources were filtered out) and isn't just a helper class.

    RELNOTES: none
    PiperOrigin-RevId: 174049618