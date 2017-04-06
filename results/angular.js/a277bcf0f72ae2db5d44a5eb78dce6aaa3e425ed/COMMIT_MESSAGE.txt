commit a277bcf0f72ae2db5d44a5eb78dce6aaa3e425ed
Author: Josh Duff <me@JoshDuff.com>
Date:   Tue Dec 29 11:51:23 2015 -0600

    refactor(equals): Cleanup `equals` function for readability

    - Removes unnecessary nested if condition
    - Simplify check when one argument is a regex

    Closes: #13650