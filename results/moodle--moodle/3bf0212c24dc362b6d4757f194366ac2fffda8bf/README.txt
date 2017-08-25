commit 3bf0212c24dc362b6d4757f194366ac2fffda8bf
Author: gustav_delius <gustav_delius>
Date:   Mon Feb 6 23:25:15 2006 +0000

    Another improvement on regrading. The code thought that grading for a question changed if the grade was like 1/3. The precision in the database is different than the newly calculated grade, so now we round both values to 5 decimal places, which should fix this problem. This was committed to STABLE  by Julian a long time ago