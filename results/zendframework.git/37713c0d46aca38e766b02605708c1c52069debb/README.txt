commit 37713c0d46aca38e766b02605708c1c52069debb
Author: Ralph Schindler <ralph.schindler@zend.com>
Date:   Tue Jul 12 11:47:52 2011 -0500

    Zend\Di improvements:
      - Altered InstanceManager such that properties becomes "configuration"
      - Configuration object can configure instance parameters and methods
      - DependencyInjector can resolve a variety of situations due to instance configurations
      - InstanceCollection is removed as alternate implementations are highly unlikely