commit d4102e1ac307a14a3948b748c00c956de54873ab
Author: Tobias Lindaaker <tobias@thobe.org>
Date:   Fri Mar 9 15:46:20 2012 +0100

    Fix for an issue where store files could be closed with id generators in a bad state, leading to truncation of much of the store file.
    Also includes some improvements to test infrastructure in order to properly test this scenario.