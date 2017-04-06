commit 59aef482815f3ceaa671685db4070a67971978a2
Author: Georgios Kalpakas <g.kalpakas@hotmail.com>
Date:   Thu Feb 18 01:32:54 2016 +0200

    refactor(ngMock): make `ngMock` minification-safe

    It is not common, but some workflows result in `angular-mocks` being minified.

    Fixes #13542

    Closes #14073