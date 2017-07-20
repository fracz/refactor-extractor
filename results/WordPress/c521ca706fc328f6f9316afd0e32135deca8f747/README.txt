commit c521ca706fc328f6f9316afd0e32135deca8f747
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Fri Feb 12 19:03:26 2016 +0000

    Improve Automated Feed Tests

    Multiple improvements to the RSS2 automated tests along with the addition of Atom tests.
    1. General whitespace cleanup (since the rss2 file serves as the base of the atom file).
    2. Adds an author and category to the tests.
    3. Since the content of the posts is the same, we don't need to test all of the post content.
    4. Adds many posts so that the post count can be checked

    Props stevenkword
    Fixes #35160.


    Built from https://develop.svn.wordpress.org/trunk@36519


    git-svn-id: http://core.svn.wordpress.org/trunk@36486 1a063a9b-81f0-0310-95a4-ce76da25c4cd