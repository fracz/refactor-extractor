commit 236b0305e88adbf51fbe3da82a2730263ab8fd51
Author: Chris Beams <cbeams@vmware.com>
Date:   Mon Jul 18 21:23:55 2011 +0000

    Refactor execution of config class enhancement

    This change returns the invocation order of
    ConfigurationClassPostProcessor#enhanceConfigurationClasses to its
    pre-3.1 M2 state. An earlier (and now unnecessary) refactoring in
    service of @Feature method processing caused the change that this now
    reverts.