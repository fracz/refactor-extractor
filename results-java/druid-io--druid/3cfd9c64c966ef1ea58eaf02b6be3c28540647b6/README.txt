commit 3cfd9c64c966ef1ea58eaf02b6be3c28540647b6
Author: Himanshu <g.himanshu@gmail.com>
Date:   Thu Apr 21 19:12:58 2016 -0500

    make singleThreaded groupBy query config overridable at query time (#2828)

    * make isSingleThreaded groupBy query processing overridable at query time

    * refactor code in GroupByMergedQueryRunner to make processing of single threaded and parallel merging of runners consistent