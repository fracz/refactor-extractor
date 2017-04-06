commit 260a929e3bc1166c4f60bb15f6e9d9a829667393
Author: javanna <cavannaluca@gmail.com>
Date:   Mon Aug 17 16:22:14 2015 +0200

    [TEST] inject a random index to TestClusterService in BaseQueryTestCase#init

    Some of our next queries to refactor rely on some state taken from the cluster state. That is why we need to mock cluster service and inject an index to it, the index that we simulate the execution of the queries against. The best would be to have multiple indices actually, but that would make our setup a lot more complicated, especially given that IndexQueryParseService is still per index. We might be able to improve that in the future though, for now this is as good as it gets.