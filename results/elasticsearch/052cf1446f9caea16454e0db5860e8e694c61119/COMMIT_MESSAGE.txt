commit 052cf1446f9caea16454e0db5860e8e694c61119
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Jul 20 20:27:36 2015 -0400

    Remove Environment.homeFile()

    Today we grant read+write+delete access to any files underneath the home.
    But we have to remove this, if we want to have improved security of files
    underneath elasticsearch.