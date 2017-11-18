commit 51063441d3ed4dec8a96794f085028b4b8feb20c
Author: Matthias J. Sax <matthias@confluent.io>
Date:   Fri Oct 6 17:48:34 2017 -0700

    KAFKA-5362; Follow up to Streams EOS system test

     - improve tests to get rid of calls to `sleep` in Python
     - fixed some flaky test conditions
     - improve debugging

    Author: Matthias J. Sax <matthias@confluent.io>

    Reviewers: Damian Guy <damian.guy@gmail.com>, Bill Bejeck <bill@confluent.io>, Guozhang Wang <wangguoz@gmail.com>

    Closes #3542 from mjsax/failing-eos-system-tests