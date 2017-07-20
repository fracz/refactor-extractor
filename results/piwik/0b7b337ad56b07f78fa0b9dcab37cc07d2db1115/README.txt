commit 0b7b337ad56b07f78fa0b9dcab37cc07d2db1115
Author: Thomas Steur <tsteur@users.noreply.github.com>
Date:   Wed Apr 8 13:27:40 2015 +1200

    If the current user is already authenticated, do not authenticate the user again

    This will improve performance. For example `reloadAccess` can otherwise cause fetching all sites the user has permission to over and over again.