commit 8b47da66ec8f86a26dcb345621181ee30a59538b
Author: Henrik Nyman <henrik.nyman@neotechnology.com>
Date:   Wed Jun 15 19:32:35 2016 +0200

    Create multi realm auth manager

    Big refactoring of enterprise auth manager
    - Add a new MultiRealmAuthManager that will eventually replace
    ShiroAuthManager that does not inherit BasicAuthManager
    - Add LdapRealm configurable from SecuritySettings
    - Add EnterpriseUserManager interface
    - Let internal FileUserRealm implement EnterpriseUserManager

    We still need to move some of the login logic into FileUserRealm