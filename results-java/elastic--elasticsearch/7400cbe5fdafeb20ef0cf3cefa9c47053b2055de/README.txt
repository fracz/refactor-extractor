commit 7400cbe5fdafeb20ef0cf3cefa9c47053b2055de
Author: Adrien Grand <jpountz@gmail.com>
Date:   Mon Oct 5 18:49:53 2015 +0200

    Remove UpdateTests' dependency on groovy.

    This test had to be moved to lang-groovy when groovy has been made a plugin.
    I refactored it a bit to use mock plugins instead so that groovy is not
    necessary anymore and it can come back to core.