commit 83824089f2371d6375c2713d494f276aec27c1a1
Author: Matthias J. Sax <matthias@confluent.io>
Date:   Tue Mar 21 19:18:41 2017 -0700

    MINOR: improve hanging ResetIntegrationTest

    Sometimes `ResetIntegrationTest` hangs and thus the build times out. We suspect, that this happens if no data is written into the input topics. Right now, input data is written once and reused for both test cases. If for some reason, the broker gets recreated (between both test cases), no data will be available for the second test method and thus the test hangs.

    This change ensures, that input data is written for each test case individually.

    Author: Matthias J. Sax <matthias@confluent.io>

    Reviewers: Ismael Juma, Eno Thereska, Guozhang Wang

    Closes #2630 from mjsax/minor-reset-integration-test