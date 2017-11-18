commit 74a7b34c01a21ed115d3573dd08e2033d17bd101
Author: Cory Roy <corymroy@gmail.com>
Date:   Tue Apr 25 15:35:01 2017 -0700

    Bump gradle version (#79)

    * bump version of gradle to 2.3.1 and gradle wrapper to 3.3

    * fix lint issues by inheriting from AppCompatTextView

    * remove unused import

    * refactor pager adapter to use hashmap rather than fragment manager to find existing fragments

    * fix code-style issues

    * fix modre code-style issues

    * change foreach style

    * change foreach to old for style

    * remove reference to unused fragment manager

    * remove unnecessary lines from gitignore

    * change position of nullable annotations

    * fix config change issues with adapter refactor

    * remove unused code in activity

    * fix some code-style issue. Thanks milkrun

    * Add some more null checks to handle filter changes when viewmodel not yet populated