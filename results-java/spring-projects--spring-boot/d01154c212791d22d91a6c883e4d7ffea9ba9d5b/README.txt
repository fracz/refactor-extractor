commit d01154c212791d22d91a6c883e4d7ffea9ba9d5b
Author: Stephane Nicoll <snicoll@gopivotal.com>
Date:   Wed Jul 9 18:15:35 2014 +0200

    Move Spring Social and Spring Mobile configuration

    This commit uses dedicated Properties classes instead of accessing
    the raw environment for Spring Social and Spring Mobile. This
    improves the readability and the discovery of such properties.

    Fixes gh-1238