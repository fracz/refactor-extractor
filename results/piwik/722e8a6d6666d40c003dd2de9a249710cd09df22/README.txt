commit 722e8a6d6666d40c003dd2de9a249710cd09df22
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Oct 22 22:40:18 2009 +0000

    - Fixes #708 kudos to Maciej for his work on this one, and Anthon & Matt for the review.
    Changes from Maciej's patch:
    -- the old Actions stats are displayed in the Page titles section, ensure BC when users were specifying page titles, they would appear in the same report. The api Actions.getActions (now deprecated) is a proxy to Actions.getPageTitles)
    -- applied schema change to log_conversion
    -- empty action names and empty URLs are now accepted at tracking time. They will be replaced at archiving time by the strings defined in the config file.
    - API: if a method has a comment @deprecated, it won't be shown in the API listing page (used for the now deprecated Actions.getActions() API call)
    - Fixes #693 Visits generator now asks for user confirmation before inserting fake data in the DB

    TODO:
    - apply small modifs to the JS tracker (setCustomUrl() and action_name defaulting to document.title)
    - improve/fix unit tests around the expected behavior of Action naming

    git-svn-id: http://dev.piwik.org/svn/trunk@1530 59fd770c-687e-43c8-a1e3-f5a4ff64c105