commit 87aca7b20a8cf2826d6d6169695101628c004024
Author: Davide Grohmann <davide.grohmann@neotechnology.com>
Date:   Thu Dec 3 15:24:34 2015 +0100

    Handle store apply events correctly

    The refactored code was missing to call the `beginStoreApply` before
    starting apply commands to the store.