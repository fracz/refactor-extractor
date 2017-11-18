commit 996c81e278ada1e4af3d000dc99039157fc92599
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Mar 16 14:46:23 2012 +0400

    Git CherryPicker refactoring

    Collect all data not to separate lists and maps, but to a list of CherryPickedData.
    Pre-initialize and save instance of myCheckinEnvironment, myChangeListManager.
    Pass GitCommit itself to cherryPickStep().