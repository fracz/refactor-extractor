commit 84990ad60873706f4abf3780ac3bb00553854216
Author: Andy Clement <aclement@vmware.com>
Date:   Fri Aug 15 00:25:23 2008 +0000

    Parser error message improvements: grammar tweak to disallow 0xF00G (was treated as number then property reference - even without a dot)