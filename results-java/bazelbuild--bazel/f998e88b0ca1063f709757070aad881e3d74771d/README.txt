commit f998e88b0ca1063f709757070aad881e3d74771d
Author: Peter Schmitt <schmitt@google.com>
Date:   Thu Feb 26 22:20:02 2015 +0000

    Actually use bundles attribute in objc_import.

    This is left over from the abstract rules refactor where I added this
    attribute to objc_import but not actually any code to use it.

    --
    MOS_MIGRATED_REVID=87284690