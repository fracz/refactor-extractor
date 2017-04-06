commit 6f5fc557ad47184571942a7f0753af1b8f30c739
Author: Igor Minar <igor@angularjs.org>
Date:   Wed Aug 13 16:02:21 2014 -0700

    refactor(jqLite): simplify jqLiteAddNodes when multiple nodes are being added

    This branch is not as hot as the single node branch and the slice that we need for old webkit/phantom makes for loop preferable.