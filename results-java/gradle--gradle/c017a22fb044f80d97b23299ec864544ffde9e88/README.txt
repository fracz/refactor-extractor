commit c017a22fb044f80d97b23299ec864544ffde9e88
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Tue May 8 14:49:44 2012 +0200

    GRADLE-2228, Fixed the problem with unnecessary daemon processes.

    1. The root cause was that matching of the java home location was not symlink-safe. Now it is symlink safe I canonicalize it explicitly.
    2. Unnecessary daemons problem won't happen again because now I'm actually validating the criteria with the newly spawned daemon. If the daemon does not match I fail early. This way it's going to be easier to figure out the problem as one don't have to parse the logs, assume things, etc.

    All the changes are subject to change sometime soon as we improve the daemon gradually. For example, if the new daemon has context that does not match the parent we should stop this daemon.