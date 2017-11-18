commit 51fb84762f9ce9d245e1bbc13c9fdb788d976e3d
Author: Roman Shevchenko <roman.shevchenko@jetbrains.com>
Date:   Tue Jan 26 16:06:48 2016 +0300

    [platform] extended file name validity check (IDEA-150199)

    - methods in PathUtil[Rt] extended to exclude reserved Windows names, and verify that the name may be encoded with platform's charset (Unix-only)
    - name validity checks in VFS refactored to introduce VirtualFileSystem.isValidName()
    - LocalFileSystem.isValidName() uses platform rules