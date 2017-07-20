commit de8e7d8b01175c3a8e370f27a377154eea9668ef
Author: phpnut <phpnut@cakephp.org>
Date:   Fri Nov 23 08:16:23 2007 +0000

    "Closes #1854, Core.po not loaded when using /app/locale/..../default.po
    Closes #3539
    Fixes #3611, Cannot redeclare loadmodels
    Fixes #3622, loadControllers() and loadModels() look in wrong folders for AppController resp. AppModel
    Added trigger_error to all deprecated functions in basics.php
    Refactored I18n class to remove debug_backtrace() usage in basics.php, all translations should be placed in a default.po or .mo file from this point forward.
    _ _d() function should be used if domain specific translations are used.
    Core translations can be placed in app/locales/{lang}/LC_MESSAGES/core.po or .mo these will now be merged with the specified language strings from default.po will replace the core message strings
    All translations are now cached to improve performance.
    "

    git-svn-id: https://svn.cakephp.org/repo/branches/1.2.x.x@6065 3807eeeb-6ff5-0310-8944-8be069107fe0