commit 012ab1f8745c8985d3f132c2dfa8fd84e7dc7041
Author: rodyhaddad <rody@rodyhaddad.com>
Date:   Thu Jul 10 11:19:18 2014 -0700

    fix(jqLite): correctly dealoc svg elements in IE

    SVG elements in IE don't have a `.children` but only `.childNodes` so it broke.
    We started using `.children` for perf in e35abc9d2fac0471cbe8089dc0e33a72b8029ada.

    This also acts as a perf improvements, since
    `getElementsByTagName` is faster than traversing the tree.

    Related #8075