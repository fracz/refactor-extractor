commit fbf36957e671721cc20cf76f16aeaab2723cd045
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jun 8 08:07:12 2011 +0200

    refactored Doctrine Bridge

     * added a RegistryInterface

     * changed all classes to depend on the Registry instead of a specific EntityManager

    This is more consistent as the validator already took the registry and this allows
    to use any entity manager in Forms.