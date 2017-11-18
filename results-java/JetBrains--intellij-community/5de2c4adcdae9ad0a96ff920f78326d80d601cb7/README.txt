commit 5de2c4adcdae9ad0a96ff920f78326d80d601cb7
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Mar 21 14:44:24 2016 +0300

    [patch]: provide remained path files from dialog to executors and rename parameter

    * improve remove unshelved strategy;
    * remove unnecessary equals and hashcode methods from FilePatch because thery can't be equal only by paths;
    * avoid double patchFile reading in UnshelvePatchDefaultExecutor, use already read information from ChangeList;