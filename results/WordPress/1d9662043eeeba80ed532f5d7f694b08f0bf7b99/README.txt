commit 1d9662043eeeba80ed532f5d7f694b08f0bf7b99
Author: lancewillett <lancewillett@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Thu Aug 2 19:12:11 2012 +0000

    Twenty Twelve: CSS and markup improvements for better child theme support, part 3. See #21379.

     * Add classes like `.site` and `.site-content` in addition to the set of IDs already present, making things much better for child themes to have more than one generic element like `nav` inside the content container.
     * Bump JS version after selector change.
     * Move `image-attachment` to `post_class()` output

    More exhaustive notes in the ticket on each id and class change.


    git-svn-id: http://core.svn.wordpress.org/trunk@21404 1a063a9b-81f0-0310-95a4-ce76da25c4cd