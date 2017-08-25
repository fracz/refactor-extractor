commit 53354ec03c170b87d745adaf518ed96543f4cb65
Author: mark-nielsen <mark-nielsen>
Date:   Mon May 1 02:56:06 2006 +0000

    Due to changes in Quiz which allows commenting and manual grading of all question types, essay no longer needs its two tables: question_essay and question_essay_states

    backuplib.php removed backup code related to question_essay_states
    type/essay/db/mysql.sql and postgres7.sql removed all SQL
    type/essay/display.html improved essay display
    type/essay/questiontype.php, editquestion.php, and editquestion.html removed all uses
    of the two tables - this resulted in a great simplification of the essay question type