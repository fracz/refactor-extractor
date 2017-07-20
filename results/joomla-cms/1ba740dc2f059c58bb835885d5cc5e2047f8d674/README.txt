commit 1ba740dc2f059c58bb835885d5cc5e2047f8d674
Author: Louis Landry <louis.landry@joomla.org>
Date:   Thu Jan 12 11:45:38 2006 +0000

    Phase 1 of com_content refactor complete
    Deprecated mosErrorAlert, use josErrorAlert
    Implemented static template array in JApplication->getTemplate
     -- This reduced the number of queries on some pages by over 20

    git-svn-id: http://joomlacode.org/svn/joomla/development/trunk@1756 6f6e1ebd-4c2b-0410-823f-f34bde69bce9