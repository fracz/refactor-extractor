commit e06653c6c6783d0fa59a21a8c52d6e0bc55bea7d
Author: David Grudl <david@grudl.com>
Date:   Fri Aug 8 16:10:40 2008 +0000

    - Nette::Component - removed method constructed(), added argument $need to lookup() & lookupPath()
    - Nette::ComponentContainer - improved addComponent() and getComponents(), removed notifyComponents()
    - Nette::Web::Html - removed $parent