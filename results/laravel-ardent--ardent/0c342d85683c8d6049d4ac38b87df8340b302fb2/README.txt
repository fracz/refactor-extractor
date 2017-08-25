commit 0c342d85683c8d6049d4ac38b87df8340b302fb2
Author: Igor Santos <igorsantos07@gmail.com>
Date:   Mon Jul 15 02:31:15 2013 -0300

    Hooking before/after methods for create, update, validate and delete to model events

    - removal of blank before/afterSave methods, as having blank methods for all hooks would clutter the class
    * closures passed as arguments to save/forceSave now get hooked as another function to saving/saved events [BREAKING]
    * forceSave() now calls save(), that have an additional $force argument [DRY]
    + minor improvements on methods documentation