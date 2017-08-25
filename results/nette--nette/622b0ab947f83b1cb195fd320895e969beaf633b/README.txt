commit 622b0ab947f83b1cb195fd320895e969beaf633b
Author: David Grudl <david@grudl.com>
Date:   Tue Nov 18 13:17:43 2008 +0000

    - Presenter refactoring (removed $globalComponents, registerComponent(), unregisterComponent)
    - added component factory method ComponentContainer::createComponent
    - ComponentContainer::getComponent() accepts name-path as argument