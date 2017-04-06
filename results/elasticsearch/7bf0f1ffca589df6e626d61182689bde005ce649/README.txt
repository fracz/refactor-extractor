commit 7bf0f1ffca589df6e626d61182689bde005ce649
Author: kimchy <kimchy@gmail.com>
Date:   Fri Mar 5 01:39:04 2010 +0200

    refactor client api, remove execXXX, and simple remain with the actual operation name as the method name, one that returns a future, and one that accepts a listener