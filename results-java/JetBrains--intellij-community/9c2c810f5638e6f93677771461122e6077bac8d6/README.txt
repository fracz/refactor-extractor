commit 9c2c810f5638e6f93677771461122e6077bac8d6
Author: Vyacheslav Lukianov <lvo@jetbrains.com>
Date:   Wed Jul 5 18:01:55 2006 +0400

    refactored:
    - AntProject.getImports() is obsolele
    - AntPsiUtil.getImportedFiles() moved to the AntProject and made instance method
    - getImportedFiles() doesn't enumerate ant children (uses only xml facilities) since it can be called on the construction of PSI