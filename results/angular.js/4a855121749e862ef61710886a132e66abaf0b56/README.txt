commit 4a855121749e862ef61710886a132e66abaf0b56
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Mon Oct 13 19:04:00 2014 +0200

    refactor($browser): more test coverage around history.state manipulation

    Check that pushState is not invoked if $browser.url() and $browser.state()
    is passed to $browser.url setter.

    Also, a minor refactor in $browser.url code and $browser specs.

    Refs #9587