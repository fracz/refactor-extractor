commit c557663b90dc3ac3f0ae7391cb6eac543812a946
Author: Tanguy Leroux <tlrx.dev@gmail.com>
Date:   Fri Jun 24 16:51:49 2016 +0200

    Make discovery-azure work again

    The discovery-plugin has been broken since 2.x because the code was not compliant with the security manager and because plugins have been refactored.

    closes #18637, #15630