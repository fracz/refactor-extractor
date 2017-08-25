commit 2badf2e6ecbe4a26a8fccba81deeeedb53453389
Author: jamiesensei <jamiesensei>
Date:   Sat May 3 13:06:49 2008 +0000

    MDL-14677 "should rescale the grades displayed for each question in the detailed marks view of the overview report so that all grades add up to the sum total grade" used quiz_rescale_grades function to rescale the grade properly before displaying it. Also this patch includes some general clean up and refactoring of overview report.