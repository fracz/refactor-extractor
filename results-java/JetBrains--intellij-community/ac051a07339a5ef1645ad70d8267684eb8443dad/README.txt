commit ac051a07339a5ef1645ad70d8267684eb8443dad
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Fri Jun 24 18:52:24 2016 +0300

    [patch]: improve apply patch ux in case of Abort... during Merge or Exceptional situations

    * do not show annoying dialog twice when abort during merge;
    * always rollback without confirmation when ApplyStatus is Abort;
    * improve message text;