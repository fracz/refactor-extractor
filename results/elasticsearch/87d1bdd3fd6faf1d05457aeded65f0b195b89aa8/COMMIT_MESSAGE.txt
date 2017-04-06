commit 87d1bdd3fd6faf1d05457aeded65f0b195b89aa8
Merge: d7884b6 7a7c7f4
Author: Isabel Drost-Fromm <isabel.drostfromm@elastic.co>
Date:   Mon May 18 09:48:36 2015 +0200

    Merge pull request #11005 from MaineC/feature/span-term-query-refactoring

    Refactors SpanTermQueryBuilder.

    Due to similarities with TermQueryBuilder a lot of code was moved into separate abstract classes that can be used by both - TermQueryBuilder and SpanTermQueryBuilder.

    Relates to #10217