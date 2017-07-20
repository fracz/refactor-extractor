commit 8913bee15a22721800069587bdf15a978b90b082
Author: Carsten Brandt <mail@cebe.cc>
Date:   Thu Dec 8 15:07:33 2016 +0100

    refactored pagecache test

    - use dataprovider for different test cases instead of foreach
    - use time mocking to avoid sleep(2)