commit 83f1b5fa35aa80852902e3bc623fb68fa97f6d3a
Author: Trustin Lee <trustin@gmail.com>
Date:   Sun May 20 16:29:31 2012 +0900

    Implement spinning in SelectorUtil.select()

    - this seems to improve performance when the number of connections is
      very small