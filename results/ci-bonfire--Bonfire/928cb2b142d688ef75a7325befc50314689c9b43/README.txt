commit 928cb2b142d688ef75a7325befc50314689c9b43
Author: Mat Whitney <mwhitney@mail.sdsu.edu>
Date:   Wed Aug 29 14:08:42 2012 -0700

    Assets library: refactored find_files/combine_css

    combine_css: removed $type from initial if statement since function
    would return if count(files) == 0 regardless of the value of $type.

    find_files: moved a chunk of repeated logic to get_file_array function
    and modified find_files to use the new function