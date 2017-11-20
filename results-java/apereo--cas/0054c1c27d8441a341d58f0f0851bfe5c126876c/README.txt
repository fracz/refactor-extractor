commit 0054c1c27d8441a341d58f0f0851bfe5c126876c
Author: Misagh Moayyed <mmoayyed@unicon.net>
Date:   Wed Apr 9 00:44:10 2014 -0700

    CAS-1438: URL matching support for proxy validation

    https://issues.jasig.org/browse/CAS-1438

    This commit in general intends to improve verification of service urls in terms of encoding and case upon validation requests. It presents the following changes:

    1. An option for regex service to allow case-insensitive matching. By default case-sensitivity is turned on for backwards compatibility purposes.
    2. When checking a ST to see if its issuance was against the same requesting service, the comparison of ids (urls) is now performed in a case-insensitive manner. Both ids (urls) are also first decoded before checking for equality in order to resolve encoding issues with %20, +, and likely other differences in encodig impls.
    3. Test cases to cover all above scenarios.