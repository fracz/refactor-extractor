commit b92241f2805d2907c931b60a2829ac02d45dbde1
Author: Marina Glancy <marina@moodle.com>
Date:   Thu Apr 19 10:40:21 2012 +0800

    MDL-31901: Changing interface of file picker, use renderers:

    - Filepicker window is now resizable and draggable;
    - filepicker&renderer: removed id substitution from renderer, replaced it with classes (better readability);
    - filepicker: allowed tree and table view for search results;
    - filepicker: fixed small bug with displaying external link checkbox
    - filepicker: display additional information about file in select window, repository returns more formatted information