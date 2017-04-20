commit e733e5cee8585fdf4a0c0e5e77e7816befa24ad4
Author: chengchao <defcc@users.noreply.github.com>
Date:   Tue Mar 21 09:50:26 2017 +0800

    fix SSR v-show render. (#5224)

    * fix SSR v-show bug. v-show info needs to be merged from parent to child component

    * improve variable name

    * update test case

    * update test case