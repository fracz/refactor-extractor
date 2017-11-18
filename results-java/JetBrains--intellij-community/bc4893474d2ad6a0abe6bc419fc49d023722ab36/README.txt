commit bc4893474d2ad6a0abe6bc419fc49d023722ab36
Author: nik <Nikolay.Chashnikov@jetbrains.com>
Date:   Thu May 25 14:15:49 2017 +0300

    tests refactoring: extracted base class from DirectoryIndexTest

    DirectoryIndexTest is too big now and it's better to write new tests in separate classes. Also initializing the whole directory structure in 'setUp' method looks overcomplicated, because many test methods use only some of these directories.