commit 6db173ccb389309a547e7c1ed4150c78d42cee2b
Author: Matthew Weier O'Phinney <matthew@zend.com>
Date:   Thu Aug 25 16:56:18 2011 -0500

    ZF-1598: protected members no longer need leading underscores

    - Reverted the part of the refactor that added a leading underscore
    - Should fix these throughout the framework