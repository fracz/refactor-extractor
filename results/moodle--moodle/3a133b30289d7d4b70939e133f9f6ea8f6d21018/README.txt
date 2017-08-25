commit 3a133b30289d7d4b70939e133f9f6ea8f6d21018
Author: Tobias Reischmann <tobias.reischmann@wi.uni-muenster.de>
Date:   Thu Jun 8 11:56:03 2017 +0200

    MDL-54965 mod_data: removed unused code in display_add_field()

    Since the refactoring towards using the filepicker for file and picture
    fields there are several variables, which are set but never read. Thus,
    I removed them.