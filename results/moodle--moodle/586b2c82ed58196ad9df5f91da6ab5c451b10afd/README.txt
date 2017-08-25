commit 586b2c82ed58196ad9df5f91da6ab5c451b10afd
Author: kaipe <kaipe>
Date:   Sun Aug 3 23:00:45 2003 +0000

    New quiz option - "Each attempt builds on the last"
    This makes it possible for students to take a tedious quiz, save it half-way and have it graded. The student can then, at a later point, get back to the quiz and have the previous answers already filled in and graded. The student can then continue with the remaining questions as well as redo all the answers that got wrong at the previous attempt.
    It seems to work fine with one little twisted exception:
    Say that the student attempts the quiz first and that the teacher thereafter edits the quiz and removes or adds a few questions. This will work out fine for as long as the teacher do not get the idea of adding a question with question type RANDOM. The quiz will be fully functional again after removing that RANDOM question or resetting the option 'Each attempt builds on the last" to NO.
    Not a very serious problem but it takes someone with greater insight in question type RANDOM to resolve it.

    As always, I can not commit lang/en/quiz.php.
    ---
    As I was using the function quiz_get_attempt_responses I had it refactored removing the obsolete argument $quiz. I also changed the call from review.php