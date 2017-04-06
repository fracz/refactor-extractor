commit b0571517c5db009084f93fc70927a16b4ba01d20
Author: Igor Minar <igor@angularjs.org>
Date:   Sat Aug 16 22:59:12 2014 -0700

    refactor(isArray): use Array.isArray exclusively

    IE9 supports Array.isArray so we don't need a polyfill any more.