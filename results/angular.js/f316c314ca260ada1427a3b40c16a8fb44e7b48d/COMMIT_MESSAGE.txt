commit f316c314ca260ada1427a3b40c16a8fb44e7b48d
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Fri Sep 6 14:48:18 2013 +0200

    refactor($ngAnimate): simplify the animation event rewrite

    To avoid code duplication, use single variables for keeping
    properties/events names to use. Also, fix some errors that have
    happened after the rewrite from moment ago.