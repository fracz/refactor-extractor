commit 3e6669000f082808999a7216b00c4b0f5a94e1da
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Mon May 15 11:26:08 2017 -0700

    MINOR: Eliminate PID terminology from non test code

    Producer id is used instead.

    Also refactored TransactionLog schema code to follow
    our naming convention and to have better structure.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Guozhang Wang <wangguoz@gmail.com>, Jason Gustafson <jason@confluent.io>

    Closes #3041 from ijuma/eliminate-pid-terminology