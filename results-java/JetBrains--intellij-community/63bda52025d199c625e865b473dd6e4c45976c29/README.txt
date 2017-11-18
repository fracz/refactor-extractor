commit 63bda52025d199c625e865b473dd6e4c45976c29
Author: Bas Leijdekkers <leijdekkers@carp-technologies.nl>
Date:   Mon Dec 21 15:22:54 2009 +0100

    do not use CodeStyleManager.reformat in "Replace If with Switch Statement" and "Replace Switch with If Statement" to improve performance (10x in some cases)