commit 71db26e8402501b5d4e953facf41f4c5760060fc
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sat May 5 14:13:01 2012 +0200

    fixes #454: refactored writer instantiation method to first check for oldskool class names and then whether the class exists as singular name; and added a check if the writer is a subclass of phpDocumentor_Transformer_Writer_Abstract (writers must subclass this class)