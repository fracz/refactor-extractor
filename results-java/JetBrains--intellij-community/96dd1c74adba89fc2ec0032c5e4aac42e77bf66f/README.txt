commit 96dd1c74adba89fc2ec0032c5e4aac42e77bf66f
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Wed May 17 14:43:54 2017 +0300

    [patch]: refactoring

    * make ApplyPatchForBaseRevisionTexts.java immutable;
    * remove Warning list from ApplyPatchForBaseRevisionTexts, use LOG.warn;
    * extract applying onto baseContent, found base, local as methods;
    * do not apply patch onto wrong base someHow: if base is presented then
    patch has to be applied exactly otherwise -> warn;