commit 8a8a2cf4623708b69dba3816e22b01407e338b73
Author: Igor Minar <iiminar@gmail.com>
Date:   Tue Jul 12 00:47:07 2011 -0700

    refactor($browser.xhr): use $browser.addJs for JSONP

    There is no reason why we shouldn't reuse $browser.addJs for JSONP
    requests.