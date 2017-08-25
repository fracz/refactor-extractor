commit 0432c3273992cf44b711fad92d442c81016a96c1
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 18 16:07:23 2013 -0400

    [feature/auth-refactor] Make DB auth consistent with interface

    Makes provider_db consistent with provider_interface.
    Removes $ip, $browser, and $forwarded_for from the arguments of
    phpbb_auth_provider_db::login() as these are provided by the global
    variable $user.

    PHPBB3-9734