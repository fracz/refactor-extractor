commit e4edbe9521e640bd3ce720b6dff19a39343a215a
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Feb 27 14:32:08 2014 +0400

    hg action dialog refactoring

    *HgMergeDialog and HgUpdateToDialog unified using one common dialog;
    *HgPushAction extended HgAbstractGlobalAction;
    *HgRepository information used if possible;
    *unnecessary hg classes removed;
    *doValidate method implemented for common dialog;
    *annotations added