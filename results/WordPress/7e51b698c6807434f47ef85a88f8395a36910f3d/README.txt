commit 7e51b698c6807434f47ef85a88f8395a36910f3d
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Thu Jul 9 00:25:25 2015 +0000

    TinyMCE: when deleting an image, ensure the wrapping link (if any) is deleted too. Fixes the erroneous showing of the link toolbar after deleting an image. Also small readability fix.
    See #32604.
    Built from https://develop.svn.wordpress.org/trunk@33141


    git-svn-id: http://core.svn.wordpress.org/trunk@33112 1a063a9b-81f0-0310-95a4-ce76da25c4cd