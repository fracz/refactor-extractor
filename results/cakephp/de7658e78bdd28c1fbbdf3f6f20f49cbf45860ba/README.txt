commit de7658e78bdd28c1fbbdf3f6f20f49cbf45860ba
Author: Mark Story <mark@mark-story.com>
Date:   Tue Dec 8 22:58:08 2009 -0500

    Modifying FormHelper::create() and FormHelper::secure() to use hidden divs instead of hidden fieldsets.  This improves the semantics of FormHelper and allows generated html to more easily pass HTML4.0 validation.
    Test cases updated.