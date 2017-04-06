commit 0b34c73b933f4d5d29750aa3330e8a591d516ab1
Merge: 99ac708 7dcf0da
Author: Isabel Drost-Fromm <isabel.drostfromm@elastic.co>
Date:   Thu Aug 20 10:21:55 2015 +0200

    Merge pull request #12810 from MaineC/feature/simple-query-string-test

    Brings Lucene query assertions to QB test.

    Theser assertions were originally added as part of the SimpleQueryStringQueryBuilder refactoring but removed later as there were more extensive tests in place already. This commit brings them back in as the other tests have been removed.

    This relates to #10217 and #11274