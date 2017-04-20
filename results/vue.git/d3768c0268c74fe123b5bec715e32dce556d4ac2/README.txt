commit d3768c0268c74fe123b5bec715e32dce556d4ac2
Author: chengchao <defcc@users.noreply.github.com>
Date:   Tue Jan 3 00:45:42 2017 +0800

    Small refactor for template parser (#4613)

    * refactor isSpecialTag and remove useless argument of parseEndTag

    * remove const

    * remove useless isSpecialTag because template node is guarded by sfc parser.