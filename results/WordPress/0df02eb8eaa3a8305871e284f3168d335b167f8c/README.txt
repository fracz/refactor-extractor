commit 0df02eb8eaa3a8305871e284f3168d335b167f8c
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Sun Apr 13 04:02:15 2014 +0000

    Add hooks to the wpeditimage tinymce plugin and to the image details portion of the media modal.
    - Add wp.media.events which is intended to be a global media event bus.
    - Add a post-render event to the ImageDetails view that third-party code can leverage to add subviews.
    - Performance improvement for the initialization of the PostImage model.
    - A bit more markup to the image details template to make it easier to add a view to the advanced options.
    Props gcorne, fixes #27698
    Built from https://develop.svn.wordpress.org/trunk@28095


    git-svn-id: http://core.svn.wordpress.org/trunk@27926 1a063a9b-81f0-0310-95a4-ce76da25c4cd