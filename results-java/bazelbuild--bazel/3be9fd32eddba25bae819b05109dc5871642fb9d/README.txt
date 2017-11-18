commit 3be9fd32eddba25bae819b05109dc5871642fb9d
Author: Laszlo Csomor <laszlocsomor@google.com>
Date:   Wed Oct 26 09:32:54 2016 +0000

    Refactor: extract anonymous classes

    AbstractFileSystem.getFile{Input,Output}Stream
    created anonymous File{Input,Output}Stream
    objects. These hold a reference to the outer class
    instance (AbstractFileSystem). This may prevent
    memory release in case the returned objects are
    kept around even if the AbstractFileSystem
    instance could otherwise be released.

    This particular refactoring is unlikely to have
    caught any memory leaks like this, so it's not
    really necessary, but I came across it and thought
    it won't hurt and will future-prove the class
    against such leaks.

    --
    MOS_MIGRATED_REVID=137254192