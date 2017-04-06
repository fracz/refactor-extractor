commit 28273b7f1ef52e46d5bc23c41bc7a1492cf23014
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Feb 4 08:56:51 2013 -0800

    refactor(angular.copy): use slice(0) to clone arrays

    slice(0) is way faster on most browsers