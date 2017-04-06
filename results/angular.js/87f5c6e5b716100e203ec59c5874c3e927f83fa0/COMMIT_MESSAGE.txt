commit 87f5c6e5b716100e203ec59c5874c3e927f83fa0
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Mar 27 10:21:49 2012 -0700

    refactor(fromJson): always use native JSON.parse

    This breaks IE7 for which you can use polyfill:

    https://github.com/douglascrockford/JSON-js

    <!--[if lt IE 8]>
    <script src="json2.min.js"></script>
    <![endif]-->

    or

    http://bestiejs.github.com/json3/

    <!--[if lt IE 8]>
    <script src="json3.min.js"></script>
    <![endif]-->