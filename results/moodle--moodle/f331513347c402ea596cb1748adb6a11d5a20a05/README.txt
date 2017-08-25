commit f331513347c402ea596cb1748adb6a11d5a20a05
Author: Damyon Wiese <damyon@moodle.com>
Date:   Wed Apr 5 15:54:20 2017 +0800

    MDL-58220 auth_oauth2: Fix for login via linked login

    This was broken by the refactoring to always link logins. Some of the static
    variables could also be removed but I'll do that in a later issue to keep this patch small.