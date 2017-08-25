commit 762ab1cb342e00bcc6ea6007946b1cd66c03d1ca
Author: Gabor de Mooij, Buurtnerd <buurtnerd@gmail.com>
Date:   Wed Apr 6 13:02:16 2011 +0200

    refactored AssociationManager to make writer API easier to use, added support for FUSE in associations (you can now have association models like Model_Book_Page and delete() gets invoked correctly ), also added ViewManager, you can now make a view with R::view(nameofView, library,book,page ) etc..