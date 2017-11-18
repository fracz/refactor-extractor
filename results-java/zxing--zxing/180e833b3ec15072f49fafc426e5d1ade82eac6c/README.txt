commit 180e833b3ec15072f49fafc426e5d1ade82eac6c
Author: srowen <srowen@59b500cc-1b3d-0410-9834-0bbf25fbcc57>
Date:   Wed Jun 18 22:12:59 2008 +0000

    Improved approach to 1D decoding -- better use of integer math by scaling pattern ratios up to expected number of pixels, rather than the other way. Modified constants accordingly. Also introduced notion of maxium variance that any one bar in a pattern can have and stiill be accepted. Finally, adjusted false-positives test failure limit downward due to recent improvements.

    git-svn-id: https://zxing.googlecode.com/svn/trunk@441 59b500cc-1b3d-0410-9834-0bbf25fbcc57