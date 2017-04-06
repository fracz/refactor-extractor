commit d5b7803064650556a21bbe360d79a41f03b6d506
Author: Michał Gołębiowski <m.goleb@gmail.com>
Date:   Wed Oct 5 12:11:36 2016 +0200

    refactor(jqLite): deprecate bind/unbind

    The on/off aliases have been available since Angular 1.2. bind/unbind have
    been deprecated in jQuery 3.0 so we're following suit in jqLite.