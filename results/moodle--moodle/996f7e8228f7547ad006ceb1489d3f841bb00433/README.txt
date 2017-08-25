commit 996f7e8228f7547ad006ceb1489d3f841bb00433
Author: David Mudrák <david@moodle.com>
Date:   Wed Feb 24 16:49:32 2016 +0100

    MDL-50794 workshop: Improve the file type restricting implementation

    This is basically a clean up and what I think improved version of the
    original Mahmoud's patch.

    The actual checking for allowed file extensions has been re-implemented
    and is now covered by unit tests. The list of allowed extensions is now
    also assed to the filemanager element's accepted_types option to prevent
    picking other files (we still need the in-place validation though). The
    form validation is simplified a bit. The custom validation of file size
    introduced in the previous patch has been removed as not related to this
    issue (also I believe it should not be done at this level).