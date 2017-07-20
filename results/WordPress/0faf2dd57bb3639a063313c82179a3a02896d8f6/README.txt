commit 0faf2dd57bb3639a063313c82179a3a02896d8f6
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Mon Dec 14 16:07:39 2015 +0000

    Accessibility: Remove title attributes from the Network Themes list table.

    Title attributes in the "Themes" screen and in the "Edit Site" screen Themes tab
    are now replaced with `aria-label` attributes. Also, replaces string
    concatenation with `add_query_arg()` and `sprintf()` to allow translator
    comments to be properly parsed and for better code readability.

    Props SergeyBiryukov, afercia.
    Fixes #35051.
    Built from https://develop.svn.wordpress.org/trunk@35924


    git-svn-id: http://core.svn.wordpress.org/trunk@35888 1a063a9b-81f0-0310-95a4-ce76da25c4cd