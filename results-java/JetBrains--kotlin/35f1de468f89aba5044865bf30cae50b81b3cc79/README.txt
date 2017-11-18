commit 35f1de468f89aba5044865bf30cae50b81b3cc79
Author: Stepan Koltsov <stepan.koltsov@jetbrains.com>
Date:   Sun Jun 10 03:44:29 2012 +0400

    refactor compiler launcher

    * method should not modify input
    * replace String with File
    * add @NotNull and @Nullable annotations