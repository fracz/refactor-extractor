commit e5c47933a7eb16812ea5259d8437f3c348b7562e
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Thu Apr 4 21:33:23 2013 +0200

    Add i18n to ParseCommand and clean up

    During this commit I have attempted to make the ParseCommand more readable and
    at the same time add internationalization to the command. By moving all strings to
    a messages table and addressing those using a code it is possible to reduce the size,
     and increase the readability, of the configure method.