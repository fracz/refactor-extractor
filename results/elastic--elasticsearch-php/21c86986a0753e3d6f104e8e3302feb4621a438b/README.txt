commit 21c86986a0753e3d6f104e8e3302feb4621a438b
Author: Zachary Tong <zacharyjtong@gmail.com>
Date:   Tue Apr 8 12:06:03 2014 -0400

    [TEST] Fix bug where skips in setup are ignored

    A semi-recent refactor of the setup code forgot to handle skips
    that might occur in the setup.  This commit fixes the handling of those
    skips