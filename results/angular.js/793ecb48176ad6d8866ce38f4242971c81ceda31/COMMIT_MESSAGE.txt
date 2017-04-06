commit 793ecb48176ad6d8866ce38f4242971c81ceda31
Author: Igor Minar <iiminar@gmail.com>
Date:   Sun Aug 14 03:24:09 2011 -0700

    refactor(jqLite): remove jqLite show/hide support

    it turns out that even with our tricks, jqLite#show is not usable in
    practice and definitely not on par with jQuery. so rather than
    introducing half-baked apis which introduce issues, I'm removing them.

    I also removed show/hide uses from docs, since they are not needed.

    Breaks jqLite.hide/jqLite.show which are no longer available.