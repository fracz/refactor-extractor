commit 0d55298b56fcd67570374441c0bba76c0bf0d1f0
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Fri Apr 15 14:06:46 2016 +0100

    revert: refactor($compile): move setting of controller data to single location

    Reverted from commit 83a6b150201a5dc9ff13d0bc4164ea093c348718 since it caused
    the Angular Material tabs directives to break.