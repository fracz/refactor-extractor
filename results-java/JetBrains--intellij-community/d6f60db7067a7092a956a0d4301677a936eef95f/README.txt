commit d6f60db7067a7092a956a0d4301677a936eef95f
Author: Vyacheslav Lukianov <lvo@jetbrains.com>
Date:   Sat Nov 26 20:57:11 2005 +0300

    new Vault implementation using one large content.store file
    Repository self-check on load, bundle updated with new error msgs
    Repository and Vault do not own their directories, files are placed in the Lvcs root (tests brought into correspondence)
    refactorings: removed some dummy methods and obsolete tests