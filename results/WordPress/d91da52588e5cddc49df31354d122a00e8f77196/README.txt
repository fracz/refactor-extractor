commit d91da52588e5cddc49df31354d122a00e8f77196
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Fri Oct 14 22:40:28 2016 +0000

    Accessible Tags autocomplete:
    - Replace suggest.js with UI Autocomplete.
    - Use the same settings like in the editor link toolbar.
    - Abstract it and add in a new file, tags-suggest.js. Then make it a dependency for the Tags postbox(es) and Quick and Bulk Edit.
    - Add `data-wp-taxonomy` on all input elements to improve handling in the UI for custom taxonomies.

    Props afercia, azaozz.
    See #33902.
    Built from https://develop.svn.wordpress.org/trunk@38797


    git-svn-id: http://core.svn.wordpress.org/trunk@38740 1a063a9b-81f0-0310-95a4-ce76da25c4cd