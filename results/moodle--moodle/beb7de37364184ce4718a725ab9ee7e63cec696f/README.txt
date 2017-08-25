commit beb7de37364184ce4718a725ab9ee7e63cec696f
Author: David Mudrak <david@moodle.com>
Date:   Thu May 12 15:32:36 2011 +0200

    More backup-converter API improvements

    Added write_xml() helper that writes the given tree-ish structure into
    the current xml writer. Improved get_contextid() so that it can use
    indices for searching data. Added set_stash() and get_stash() helper
    methods.