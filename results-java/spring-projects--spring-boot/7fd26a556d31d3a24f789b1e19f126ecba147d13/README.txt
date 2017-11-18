commit 7fd26a556d31d3a24f789b1e19f126ecba147d13
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Wed May 28 17:22:47 2014 +0100

    Fix social property binding

    Update Spring Social auto-configurations to read properties using
    the `dashed` notation and with the appropriate prefixes. This allows
    properties to be specified in any of the relaxed forms.

    Also minor refactor to extract common logic to a new
    SocialAutoConfigurerAdapter base class.

    See gh-941