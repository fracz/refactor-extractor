commit 004c02e05c109ba44044eeedb7d7e9689f1b5b02
Author: kaipe <kaipe>
Date:   Fri Oct 31 21:27:43 2003 +0000

    An improvement asked for by koen roggemans:
    Especially for multianswer questions it is often desirable to have the grade of the question in a quiz larger than the earlier maximum of 10 points.
    The new function quiz_gradesmenu_options($defaultgrade) makes quiz question list grade selector drop-down have the maximum grade option set to the highest value between 10 and the defaultgrade of the question.