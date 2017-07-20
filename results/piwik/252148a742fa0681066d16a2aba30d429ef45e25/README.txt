commit 252148a742fa0681066d16a2aba30d429ef45e25
Author: Thomas Steur <tsteur@users.noreply.github.com>
Date:   Tue Apr 19 08:26:28 2016 +1200

    Improved plugins update API (#10028)

    * refs #7983 let plugins add or remove fields to websites and better settings api

    * * Hide CorePluginsAdmin API methods
    * More documentation
    * Added some more tests

    * improved updates API for plugins

    * better error code as duplicate column cannot really happen when not actually renaming a colum