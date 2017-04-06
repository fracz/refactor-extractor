commit 16363d8000a484b428a16eb07d8bd0f19c4b4337
Author: Vojta Jina <vojta.jina@gmail.com>
Date:   Fri Nov 4 18:33:12 2011 -0700

    refactor(ng:view, ng:include): pass cache instance into $http

    Instead of doing all the stuff in these widgets (checking cache, etc..) we can rely on $http now...