commit d2af10140a67dcf6f21f47bbbb14408cae2c33c3
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Thu Nov 11 18:58:06 2010 +0000

    forum files MDL-25167 files in the post file area were not moved when the post was moved or deleted.

    Also, I refactord some code out of forum_move_post and into a new method file_storage::move_area_files_to_new_context. I hope that is OK.