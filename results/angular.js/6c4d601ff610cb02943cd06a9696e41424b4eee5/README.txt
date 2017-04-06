commit 6c4d601ff610cb02943cd06a9696e41424b4eee5
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Aug 4 20:07:48 2014 -0700

    refactor(jqLite): drop Node.contains polyfill

    Node.contains is supported on IE5+ and all the other browsers we care about.