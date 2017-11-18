commit b49b4b81c3bfe6201b1f6e8e30a18c5844c0d0a2
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri Nov 2 20:22:42 2012 +0400

    GitVFSListener DRY refactoring

    Introduce performBackgroundOperation() and move there the code common for add and remove.