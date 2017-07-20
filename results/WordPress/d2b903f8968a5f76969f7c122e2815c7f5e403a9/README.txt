commit d2b903f8968a5f76969f7c122e2815c7f5e403a9
Author: Lance Willett <nanobar@gmail.com>
Date:   Wed Nov 19 18:12:21 2014 +0000

    Twenty Fourteen: improve post thumbnail HTML output.

     * Add `aria-hidden` attribute to reduce verbosity on archive pages.
     * Add alt text in archives to avoid confusing link texts, see #30076 for context in Twenty Fifteen.

    Props hiwhatsup, joedolson. Fixes #30144.
    Built from https://develop.svn.wordpress.org/trunk@30387


    git-svn-id: http://core.svn.wordpress.org/trunk@30384 1a063a9b-81f0-0310-95a4-ce76da25c4cd