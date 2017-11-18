commit 8f5bf7b7c8a736f67712a00cb9482a93a4122117
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Fri Feb 17 18:29:23 2017 +0300

    [shelf]: ShelveChangesManager refactoring

    * get rid of CompoundShelfFileProcessor;
    * change savePatchFile and move to Util class;
    * move ShelveSchemeManager creation to separated method;