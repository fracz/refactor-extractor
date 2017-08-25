commit e89e09d21ddda5d285cffa7066ba8ff34aaaa80e
Author: jamiesensei <jamiesensei>
Date:   Wed Apr 9 13:01:40 2008 +0000

    MDL-14283 attempt in mdl_question_states is always 1 - typo in attempt.php introduced in Tim's access rules refactor meant that attempt in quiz_attempts table was always set to 1. This meant that only one attempt was shown on the quiz/view.php page as well as causing other problems.