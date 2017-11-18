commit 88950521a864f48a0505c2d59af11cec00c6c28e
Author: Denis Zharkov <denis.zharkov@jetbrains.com>
Date:   Thu Apr 20 16:28:58 2017 +0300

    Refactor KotlinCliJavaFileManager: make it return JavaClass

    It seems to be very natural refactoring considering the
    following changes: optimizing KotlinCliJavaFileManagerImpl.findClass
    to make it read class files manually instead of requesting PSI