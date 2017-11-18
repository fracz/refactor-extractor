commit fd4ee8caed40f84366ae3c02df563483cbded3e8
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Nov 1 17:10:09 2016 +0300

    [vcs]: improve changelistChooserPanel

    * change changelistChooser model to accept ChangeLists instead of string;
    * add completion contributor with default popup after typing: if force
    completion called then tasks from task plugin will be also suggested,
    otherwise only existing changelist from combobox model;
    * add existing/new label for selected changelist;
    * cleanUp;