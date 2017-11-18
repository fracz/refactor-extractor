commit bed73446e38707c30eef23d29fe6ddc1c8bdca16
Author: Csaba Kozak <kozakcsabi@gmail.com>
Date:   Tue Oct 31 20:14:46 2017 +0100

    Fix IllegalStateException for @OrmLiteDao in view

    #2048

    This commit contains a bigger refactor as well: the previous
    implementation contained HasLifecycleMethods interface, which was
    implemented by most all the components, even if they do not have
    the specific methods. This is now changed.