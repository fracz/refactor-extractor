commit 4f26c1bc518ad8d4df609900c7b769bf19bcceb5
Author: mwhitneysdsu <mwhitney@mail.sdsu.edu>
Date:   Fri Jan 2 07:47:14 2015 -0800

    Rename libraries to uppercase first letter

    This should not impact existing installations, but improves
    compatibility with CI 3 and consistency in naming of libraries
    throughout Bonfire.

    Changing the names of other class files (e.g. controllers and models) in
    the same manner will require some code changes in the loader.