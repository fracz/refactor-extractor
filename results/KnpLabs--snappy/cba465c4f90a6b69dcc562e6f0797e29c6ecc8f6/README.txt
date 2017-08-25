commit cba465c4f90a6b69dcc562e6f0797e29c6ecc8f6
Author: Daniel Karpinski <daniel.karpinski@helpling.com>
Date:   Mon Nov 10 11:14:59 2014 +0100

    Add check exists before unlink and refactor

    Header & Footer file now only removed when option is not null and file exists.

    Fixes:
    when option['header-html'] or option['footer-html'] is null then
    unlink(): No such file or directory
    thrown on generate.