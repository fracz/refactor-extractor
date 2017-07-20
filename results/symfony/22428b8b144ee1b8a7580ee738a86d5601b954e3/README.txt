commit 22428b8b144ee1b8a7580ee738a86d5601b954e3
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jun 7 08:58:10 2011 +0200

    [DoctrineBundle] refactored Doctrine proxy cache warmer

    * removed the dependency on the Container
    * the proxy cache is now get from each entity manager configuration