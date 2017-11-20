commit 4ae358f278634f72f6d414f0d1817722b12347bf
Author: rkorn86 <rkorn86@users.noreply.github.com>
Date:   Fri Jul 22 20:58:09 2016 +0200

    LDAP improvement & bug fix (#1890)

    * Fix for 'Pool has not been initialized' error

    * Add possibility for custom userPassword LDAP-Attribute

    * Compare custom LDAP-Attribute, when using authenticated or anon search authenticator

    * Initialize LdapAuthenticationHandler manually, as the class is no managed bean anymore

    * Fix codacy indentation level issues

    * fix more codacy issues

    * All checkstyle issued solved

    * Fixed test, which is still using managed beans

    * fallback to the pooledBindAuthnHandler if no principal password attribute is defined

    * Merge ldaptive 1.2.0 pull request

    * Fixed documentation