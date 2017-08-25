commit 4fa39250e714b3ee5aa16a5f9ce8c77daa44311b
Author: Arthur Schiwon <blizzz@owncloud.com>
Date:   Thu Aug 21 17:59:13 2014 +0200

    LDAP User Cleanup: Port from stable7 without further adjustements

    LDAP User Cleanup

    background job for user clean up

    adjust user backend for clean up

    register background job

    remove dead code

    dependency injection

    make Helper non-static for proper testing

    check whether it is OK to run clean up job. Do not forget to pass arguments.

    use correct method to get the config from server

    methods can be private, proper indirect testing is given

    no automatic user deletion

    make limit readable for test purposes

    make method less complex

    add first tests

    let preferences accept limit and offset for getUsersForValue

    DI via constructor does not work for background jobs

    after detecting, now we have retrieving deleted users and their details

    we need this method to be public for now

    finalize export method, add missing getter

    clean up namespaces and get rid of unnecessary files

    helper is not static anymore

    cleanup according to scrutinizer

    add cli tool to show deleted users

    uses are necessary after recent namespace change

    also remove user from mappings table on deletion

    add occ command to delete users

    fix use statement

    improve output

    big fixes / improvements

    PHP doc

    return true in userExists early for cleaning up deleted users

    bump version

    control state and interval with one config.php setting, now ldapUserCleanupInterval. 0 will disable it. enabled by default.

    improve doc

    rename cli method to be consistent with  others

    introduce ldapUserCleanupInterval in sample config

    don't show last login as unix epoche start when no  login happend

    less log output

    consistent namespace for OfflineUser

    rename GarbageCollector to DeletedUsersIndex and move it to user subdir

    fix unit tests

    add tests for deleteUser

    more test adjustements

    Conflicts:
            apps/user_ldap/ajax/clearMappings.php
            apps/user_ldap/appinfo/app.php
            apps/user_ldap/lib/access.php
            apps/user_ldap/lib/helper.php
            apps/user_ldap/tests/helper.php
            core/register_command.php
            lib/private/preferences.php
            lib/private/user.php

    add ldap:check-user to check user existance on the fly

    Conflicts:
            apps/user_ldap/lib/helper.php

    forgotten file

    PHPdoc fixes, no code change

    and don't forget to adjust tests