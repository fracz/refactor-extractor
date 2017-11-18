commit 623e8e2da0f0eb3544243452f09a2ceffe700809
Author: Anna Kozlova <anna.kozlova@jetbrains.com>
Date:   Thu Apr 9 16:56:53 2015 +0200

    save all documents inside move handler when !myMovedFiles.isEmpty() deletes not yet moved files from VFS and thus invalidating psi during refactoring inside write action (IDEA-125679; IDEA-121168; IDEA-121090; IDEA-117963)