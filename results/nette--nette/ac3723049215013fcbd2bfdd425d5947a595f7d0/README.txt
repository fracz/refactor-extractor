commit ac3723049215013fcbd2bfdd425d5947a595f7d0
Author: David Grudl <david@grudl.com>
Date:   Wed Nov 23 05:00:21 2011 +0100

    Configurator: refactoring, container is not created in the constructor, but later in loadConfig() or getContainer(). So make sure you did not call getContainer() before loadConfig()