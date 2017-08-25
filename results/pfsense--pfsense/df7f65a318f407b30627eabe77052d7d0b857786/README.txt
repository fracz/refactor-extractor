commit df7f65a318f407b30627eabe77052d7d0b857786
Author: NewEraCracker <neweracracker@gmail.com>
Date:   Sat Sep 3 00:38:07 2016 +0100

    Fix diag_dns regressions

    After testing diag_dns behaviour some regressions have been noticed.

    1) Looking up ipv6.google.com (it only has AAAA records) doesn't work
       - gethostbyname() only supports v4, ipv6.google.com only has v6
       - this bug was recently and inadvertently introduced
    2) Results table will always show even when domain is not resolved
       - since refactoring ages ago, $resolved is an array, bad idea to replace with a string, this will cause issues
       - this piece of code was 'dead' until the recent commit has 'enabled' it again, removing it as not needed
    3) Parameters for display_host_results (see: fe74228f2a8a9abc45a580a01559518043ca8d0b for its introduction) weren't correctly updated
       - mostly a dead function, doubt this is used for anything, keeping it just in case.

    This commit fixes all aforementioned issues.