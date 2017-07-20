commit 34a6c214f7b970e6865832c2819b74f604e79491
Author: Taylor Otwell <taylorotwell@gmail.com>
Date:   Mon Oct 12 14:13:52 2015 -0500

    Tweak how implicit model binding works.

    Various improvements that make implicit bindings available in
    middleware and requests by resolving them at the same time as explicit
    bindings.