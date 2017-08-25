commit 204c640c773e707845859d103b74d64596de402d
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 18 15:57:31 2013 -0400

    [feature/auth-refactor] Make LDAP consistent with interface

    Makes the provider_ldap consistent with the provider_interface
    except for the acp() method which has not yet been finalized.
    Renames phpbb_auth_provider_ldap::user_filter to
    phpbb_auth_provider_ldap::ldap_user_filter to maintain the original
    name of the function from auth_ldap.

    PHPBB3-9734