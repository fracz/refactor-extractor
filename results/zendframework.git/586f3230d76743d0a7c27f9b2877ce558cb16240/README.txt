commit 586f3230d76743d0a7c27f9b2877ce558cb16240
Author: Evan Coury <me@evancoury.com>
Date:   Thu Dec 8 15:33:23 2011 -0700

    Remove whitespace and refactor long lines per coding standard

    - Ran: find library/Zend/Module -type f -name '*.php' | xargs sed -i 's/[[:space:]]*$//'
    - Refactored long lines to be <= 120 chars