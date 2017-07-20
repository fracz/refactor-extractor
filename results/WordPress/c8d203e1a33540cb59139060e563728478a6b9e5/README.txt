commit c8d203e1a33540cb59139060e563728478a6b9e5
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Fri Jun 17 03:02:29 2016 +0000

    Autosave: improve the notice when the sessionStorage autosave is different than the content.
    - Make it higher priority than the server autosave.
    - Change it so the editors undo and redo can be used.
    - Replace the restore link with a button.
    - Add better explanation/help.

    See #37025.
    Built from https://develop.svn.wordpress.org/trunk@37737


    git-svn-id: http://core.svn.wordpress.org/trunk@37702 1a063a9b-81f0-0310-95a4-ce76da25c4cd