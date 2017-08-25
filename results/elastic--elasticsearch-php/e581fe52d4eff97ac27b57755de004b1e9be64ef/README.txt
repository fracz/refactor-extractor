commit e581fe52d4eff97ac27b57755de004b1e9be64ef
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Tue Apr 8 12:06:03 2014 -0400

    [TEST] Fix bug where skips in setup are ignored

    A semi-recent refactor of the setup code forgot to handle skips
    that might occur in the setup.  This commit fixes the handling of those
    skips