commit 45a81b1b1054d830042d6344ba6582184ed5b2c8
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Jan 11 16:21:47 2012 +0000

    MDL-31095 quiz editing: only set quiz->grade to 0 if really necessary.

    It is only necessary when there are (non-preview) attempts. However,
    after making this change we have to ensure users cannot start attempts
    when ->grade > 0 and ->sumgrades = 0.

    Display ->grade on the order and paging tab of edit.php, so it is clear
    what is going on when you are using that tab.

    Finally, improve the error message a student gets if they try to start a
    quiz with no questions.