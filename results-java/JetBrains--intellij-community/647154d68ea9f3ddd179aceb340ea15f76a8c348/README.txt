commit 647154d68ea9f3ddd179aceb340ea15f76a8c348
Author: Sergey Savenko <sergey.savenko@jetbrains.com>
Date:   Wed Apr 29 14:45:52 2015 +0300

    EditorTextFieldCellRenderer: don't use use PLAIN_TEXT file type by default

    improves performance as no lexing is performed if file type is not set