commit 2485c4890ce82124089062be4b0ee5622b8d54b8
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Fri May 17 11:14:44 2013 +0200

    Packaging improvements & bugfixes

    * RPM: Use the ES_USER variable to set the user (same name as in the debian package
      now), while retaining backwards compatibility to existing /etc/sysconfig/elasticsearch
    * RPM: Bugfix: Remove the user when uninstalling the package
    * RPM: Set an existing homedir when adding the user (allows one to run cronjobs as this user)
    * DEB & RPM: Unify Required-Start/Required-Stop fields in initscripts