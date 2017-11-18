commit cba0094f5a9833194bf2a13965eda678bd4a05ea
Author: Matthias J. Sax <matthias@confluent.io>
Date:   Fri Apr 21 13:10:05 2017 -0700

    MINOR: improve test cleanup for Streams

     - call close() on Metrics to join created threads

    Author: Matthias J. Sax <matthias@confluent.io>

    Reviewers: Eno Thereska, Damian Guy, Guozhang Wang

    Closes #2788 from mjsax/minor-improve-test-metric-cleanup