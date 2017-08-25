commit 0af8aa689f128cbb930bed591f4ef79224049e83
Author: macjohnny <estebanmarin@gmx.ch>
Date:   Thu Jun 12 09:51:23 2014 +0200

    drastic speedup for nested ldap groups

    Changes a function call in getUserGroups to only retrieve group ids instead of objects.
    this change significantly improves performance when using owncloud with many groups, e.g. nested ldap hierarchy (1.2.840.113556.1.4.1941), since getUserGroups gets called in oc_share::getItems, which is needed for every page request.
    in my particular case, it took more than 10s to load the calendar page and more than 6s to load the file page.
    this was in an environment with 100 user groups (nested) per user. The performance was bad due to the following call stack:
    self::getManager()->getUserGroups($user)
      - getGroupObject() (executed for every group!)
         - groupExists() (resulting in many ldap-requests)
    since the groups are loaded from ldap, it is unnecessary to check whether the group exists or not.