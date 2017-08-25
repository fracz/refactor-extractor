commit f6dade1e18a1a31cb3674345ade05369ce12dba5
Author: Andras Iklody <Andras.Iklody@ncirc.nato.int>
Date:   Mon Apr 22 14:44:55 2013 +0200

    Performance tweak

    - User/Role not looked up recursively anymore for authorisation checks -
      improves performance significantly. Also, checking perm_add and
      perm_modify instead of doing a lookup in the ACL tables