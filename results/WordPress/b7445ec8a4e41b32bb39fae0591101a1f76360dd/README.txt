commit b7445ec8a4e41b32bb39fae0591101a1f76360dd
Author: Helen Hou-Sandí <helen.y.hou@gmail.com>
Date:   Thu Jul 17 03:55:15 2014 +0000

    Media Grid Attachment Details modal UI improvements:

    * Align better visually with the existing media modal and the post image edit modal.
    * Add back a link to the full attachment edit screen (post.php).
    * Add a title to the modal and move prev/next buttons next to the more-consistent close button.
    * Remove mode tabs (metadata vs. image editing) in favor of the Edit Image button.

    Still to come: responsive, IE8 testing and targeted CSS (calc() usage), general CSS cleanup.

    props ericlewis, helen, melchoyce. see #28844. fixes #28915.

    Built from https://develop.svn.wordpress.org/trunk@29204


    git-svn-id: http://core.svn.wordpress.org/trunk@28988 1a063a9b-81f0-0310-95a4-ce76da25c4cd