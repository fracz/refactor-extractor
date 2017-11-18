commit 7109775e3997a552c1a8daf33df56e634810defe
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Dec 15 20:40:03 2015 +0300

    [patch]: do best attempt to apply patch

    * store failed patches;
    * if one filePatch has FAILED status it means we need to continue but store this info;
    * process added/deleted files and mark them as failed if preCheck failed;
    * refactor path verifier;
    * notify about failed status with detailed dialog info and rollback button (not implemented yet);
    * add labels to easily distinguish 'apply patch action' in local history dialog;