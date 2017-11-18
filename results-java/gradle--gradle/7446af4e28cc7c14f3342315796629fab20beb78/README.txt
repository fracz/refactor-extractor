commit 7446af4e28cc7c14f3342315796629fab20beb78
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Jan 18 16:37:21 2013 +0100

    Module substitution for dependency resolve rules.

    - Added useTarget() method for substituting any of the target module properties when dependency resolve rule fires.
    - Reused the ModuleVersionSelectorParser, renamed it from 'ForcedModuleParser'. Reworked some code in the notation parsing to improve null handling.