commit b281015c150cb95c4205ff6a8d8945b86fc94d4b
Author: Guozhang Wang <wangguoz@gmail.com>
Date:   Mon Oct 23 16:12:54 2017 -0700

    HOTFIX: Poll with zero milliseconds during restoration phase

    1. After the poll call, re-check if the state has been changed or not; if yes, initialize the tasks again.
    2. Minor log4j improvements.

    Author: Guozhang Wang <wangguoz@gmail.com>
    Author: Damian Guy <damian.guy@gmail.com>
    Author: Jason Gustafson <jason@confluent.io>
    Author: Matthias J. Sax <matthias@confluent.io>

    Reviewers: Bill Bejeck <bill@confluent.io>, Damian Guy <damian.guy@gmail.com>, Matthias J. Sax <matthias@confluent.io>, Ted Yu <yuzhihong@gmail.com>

    Closes #4096 from guozhangwang/KHotfix-restore-only