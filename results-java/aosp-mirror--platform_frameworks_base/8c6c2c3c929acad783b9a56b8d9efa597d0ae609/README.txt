commit 8c6c2c3c929acad783b9a56b8d9efa597d0ae609
Author: Lorenzo Colitti <lorenzo@google.com>
Date:   Thu Jun 12 13:41:17 2014 +0900

    IpPrefix improvements.

    1. Allow IpPrefixes to be created from strings. In order to do
       this, factor out the code from LinkAddress which already does
       this to a small utility class in NetworkUtils.
    2. Truncate prefixes on creation, fixing a TODO.
    3. Add a toString method.
    4. Write a unit test.

    While I'm at it, make RouteInfoTest pass again, and convert it
    to use IpPrefix instead of LinkAddress.

    Change-Id: I5f68f8af8f4aedb25afaee00e05369f01e82a70b