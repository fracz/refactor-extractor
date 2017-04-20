commit 20fb79a1ea098bedda6fec8bebbb184ac51fce20
Author: Jinjiang <zhaojinjiang@me.com>
Date:   Sun May 15 03:49:18 2016 +0800

    new render mechanism (#2857)

    * updated compiler with new render functions

    * separated createElement into renderSelf & renderElement
    supported getters for text node & static root

    * adapted new element creator into render call

    * improved \$createElement

    * fixed by flow check

    * fixed ssr bugs for $createElement