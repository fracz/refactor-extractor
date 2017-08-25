commit 998999e7251064b96580de6ed03d476138891d1c
Author: Petr Skoda <skodak@moodle.org>
Date:   Sun Dec 20 16:25:41 2009 +0000

    MDL-21155 improve themechangeonurl - sesskey not required, it may be a slight CSRF problem, but on the other hand theme designers rely on this very often, fixed problems with theme in _POST