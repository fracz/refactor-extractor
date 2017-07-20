commit d1c5a385eebf064dcd919cee18d707f693a3116b
Author: Michal Čihař <michal@cihar.com>
Date:   Mon Mar 31 10:20:20 2014 +0200

    Simplify the code and improve messages

    - Use conditions rather than ternary operator, which is hard to read
      with multiple conditions.
    - Do not concatenat translated message, use format string instead.

    Signed-off-by: Michal Čihař <michal@cihar.com>