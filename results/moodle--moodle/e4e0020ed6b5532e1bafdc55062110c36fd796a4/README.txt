commit e4e0020ed6b5532e1bafdc55062110c36fd796a4
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Oct 3 14:32:34 2011 +0100

    MDL-29625 new helper function get_plugin_list_with_class.

    Based on the existing get_plugin_list_with_function. As part
    of implementing this, I refactored out a
    get_plugin_list_with_file function, which may also be useful.