commit 1e7532eff6e6655b7238a5ffd592a7634d6d9fae
Author: Jonathan Vollebregt <jnvsor@gmail.com>
Date:   Fri May 12 23:53:37 2017 +0200

    Kint_SourceParser: Performance improvements

    Inlining the various helper methods makes a bit of a difference.

    It's fugly but it just has to work fast.