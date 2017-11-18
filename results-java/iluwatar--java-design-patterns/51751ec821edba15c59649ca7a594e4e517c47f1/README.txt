commit 51751ec821edba15c59649ca7a594e4e517c47f1
Author: mahendran.mookkiah <mahendran.mookkiah@gmail.com>
Date:   Fri Aug 18 20:44:28 2017 -0400

    #587 SonarQube reports bugs reader-writer-lock and refactor

    Keeping wait() calls with in synchronized block closely to adhere
    SonarQube rules.

    Avoid nested synchronized block by extracting method.

    Added writing and reading time to simulate testing to ensure
    1) writers are waiting for globalMutex to be empty
    2) readers to confirm there is no writers.