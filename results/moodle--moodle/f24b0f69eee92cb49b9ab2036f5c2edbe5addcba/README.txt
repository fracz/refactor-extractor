commit f24b0f69eee92cb49b9ab2036f5c2edbe5addcba
Author: Marina Glancy <marina@moodle.com>
Date:   Tue Jul 31 10:54:48 2012 +0800

    MDL-34290 repository_boxnet, boxlib use request timeouts

    boxlib receives additional argument as request timeout
    repository_boxnet::get_file_by_reference respects request timeouts and downloads file into moodle only if it is image
    also some improvements to repository_boxnet source display functions;
    also do not cache result of request in retrieving of listing, user is unable to see the new files he added to box.