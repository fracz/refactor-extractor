commit 76f477c535488231f804ddb8acc8f11c83cb420b
Author: Mahesh Panchaksharaiah <maheshp@thoughtworks.com>
Date:   Fri May 19 14:29:52 2017 +0530

    ReAuthentication fixes and logging improvements for PluginAuthenticationProvider

     * Roles are assigned against the username in authenticated user
       object since username used for login can be different from the username
       in the user provided by plugin. e.g when using a authorization ldap
       plugin, admins can provide an ability to login using either `uid`, `mail`
       or `cn` but on succesful authentication a User is created where the
       username is always mapped to the `cn`.
     * A plugin erroring out should not fail authentication,
       rather should try with other plugins if available.
     * Added debug logging around authentication using plugins.