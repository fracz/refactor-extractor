commit 7fd566f4b1189596ef97b8375cdca3fc1a3e08b9
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Mon Oct 18 12:37:38 2010 +0400

    WI-3540 Text selection problem (when selecting text with Shift+Down or Shift+PageDown to the end of fil

    1. Corrected caret position in case of 'select to document end';
    2. Minor refactoring;