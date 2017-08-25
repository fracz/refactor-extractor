commit f42d2a22bc396673f6fca7b9671af7f44e3fd8d3
Author: Ankit Agarwal <ankit@moodle.com>
Date:   Thu Apr 4 16:27:34 2013 +0800

    MDL-27814 blogs: Refactoring code to use proper context and such

    Based on the decisions made in the issue, most places in blog should use site context.
    There are also minor other refactoring to support all changes made in the issue.