commit 45bc7c292629fab0324900e86a26b731026991c1
Author: Alexander Udalov <Alexander.Udalov@jetbrains.com>
Date:   Thu Sep 26 22:19:35 2013 +0400

    Delete JvmClassName.byClass, refactor JvmAnnotationNames

    Store constant class names as instances of FqName instead of JvmClassName. This
    is done to minimize usages of the method 'JvmClassName.getFqName()', since it's
    wrong and shouldn't be used