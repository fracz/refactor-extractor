commit c31e2a81f4c3e310a3b08d85e13c0ac83f1fdac6
Author: Ralph Schindler <ralph.schindler@zend.com>
Date:   Wed May 2 15:49:21 2012 -0500

    Rearchitected & reorganized Sql\Platform implementation
    Added unit tests for Sql\Platform implementation, specifically Sql Server limit/offset support
    Removed ParameterContainer interface
    [zen-35]