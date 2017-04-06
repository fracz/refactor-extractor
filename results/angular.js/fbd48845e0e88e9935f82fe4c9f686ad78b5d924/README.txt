commit fbd48845e0e88e9935f82fe4c9f686ad78b5d924
Author: Igor Minar <igor@angularjs.org>
Date:   Sun Aug 10 04:17:36 2014 -0700

    perf(ngRepeat): clone boundary comment nodes

    http://jsperf.com/clone-vs-createcomment

    most of the improvement comes from not reconcatinating the strings