commit d62b9ec65e7fc20a817c3e35e7d52f7b45445436
Author: phpnut <phpnut@cakephp.org>
Date:   Sun Oct 9 01:56:21 2005 +0000

    Merging from sandboxes

    [1079]
    Merged [1005] committed by nate but not added to core prior to release.
    Merged [1078] prior to modifying all developers sandboxes.

    [1081]
    adding view and template directories

    [1082]
    adding base files for view generator

    [1083]
    correcting all package and sub package tags for in doc blocks.
    Making sure every file in the core has doc block in them

    [1084]
    renaming working copy of latest release

    [1093]
    Added fix for associations using underscores if var $useTable is set in the associated models.
    This closes ticket #11.

    [1094]
    Fix for Ticket #24.
    The problem was tracked to a variable in View::_render();
    $loadedHelpers was being assigned a reference when it when it should not have been.

    [1096]
    Initial work on controller components needs testing.
    Also added a work around for the basics.php uses().
    Using the define DS where the files from the original version are now located in deeper libs directories.

    [1097]
    committing a few typos in the code I added

    [1098]
    reformatting code in component.php

    [1104]
    changed the test route and corrected a regex in inflector.

    [1111]
    removing the contructor from dispatcher, it is not needed

    [1112]
    Changes made for errors when a file is not present in webroot.
    Fixed the regex used in Router::parse().
    Change the error layout template.

    [1113]
    Changes to Folder class to allow setting the permissions mode when constructing.
    This class needs to be refactored and move everything that is in the contructor out.
    The constructor should set the vars for use in other Folder::"methods"().
    Will work on this at a later time.

    git-svn-id: https://svn.cakephp.org/repo/trunk/cake@1114 3807eeeb-6ff5-0310-8944-8be069107fe0