commit 098ffcd521cd272ee3a63174759cceeda618d2ef
Author: Romain Guy <romainguy@google.com>
Date:   Mon Oct 10 15:22:25 2011 -0700

    Don't call saveLayer() when it's not required
    Bug #5435653

    saveLayer() can be extremely expensive on some GPU architectures. Avoiding
    this call greatly improve the rendering performance of drawables with
    strok + fill.

    Change-Id: Ib414174ba05d5bad56d942b8e67ab784e7d60b9e