commit 8d54191d7afc22d3bc3e15f15ca5c1804f6835d2
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Jan 7 15:40:42 2013 +0000

    MDL-37374 questions: use property_exists rather than isset

    $a->field = null; isset($a->field) returns false, which is typical PHP.
    I also improve the error handling a bit.