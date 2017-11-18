commit 6dc444ee3abefb2f88e1b7f8b45cd701d919f246
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Mon Aug 16 11:02:48 2010 +0400

    WI-1431    Add spellchecker to 'Commit Changes' dialog box ('Comment' text area)

    1. Editor is used at 'Commit Changes' dialog now;
    2. Editor from 'Commit Changes' dialog' is configured to use soft wraps and spell checking inspection all the time;
    3. Added utility method PsiUtilBase.findEditor() - allows to find virtual file editor stored at GUI component properties in addition to standard lookup via file;
    4. Default soft wrap drawing sign is changed in order to sort out problem with default unicode symbols representation under Windows 7 fonts;
    5. Added ability to disable second-level options at quick fix dialog ('disable inspection', 'configure inspection settings' etc) and disabled them for spell checker inspection used at editor from 'Commit Changes' dialog;
    6. Added ability to order actions triggered by keyboard shortcut. That ability is used within editor from 'Commit Changes' dialog in order to resolve problem with 'Show Commits History' and 'Scroll To Center' actions clash;
    7. Minor refactorings;