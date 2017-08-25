commit 0633666e2b5e39a7ebf7d2a68dc4c1b4dbbc0db1
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Thu Jun 20 16:46:25 2013 -0400

    [feature/auth-refactor] Fix LDAP conversion error

    I messed up when converting over auth_ldap this commit fixes that
    error. I have not been able to extensively test ldap due to not
    having ldap set up on this computer yet.
    Apache authentication appears to work.

    PHPBB3-9734