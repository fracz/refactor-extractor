commit 90e670021da9d524b18e1d85eaca0caacea6b7a5
Author: robocoder <anthon.pang@gmail.com>
Date:   Fri Aug 21 04:49:05 2009 +0000

    LanguagesManager setLanguageForUser() and setLanguageForSession(): rename $language param to $languageCode.

    Piwik::isPhpCliMode() deprecated; moved to Piwik_Common::isPhpCliMode()

    Piwik_Common::getBrowserLanguage() uses $_ENV['LANG'] if PHP CLI

    Fixes #749 - Login form changes
    1) Lost password form now sends an email with reset instructions and link to a password change form instead of changing the password.
    2) Added password change form which requires input of a generated token (valid for 24 hrs).
    3) Fix $urlToRedirect handling; refactoring; phpdoc comments; minor reformatting



    git-svn-id: http://dev.piwik.org/svn/trunk@1415 59fd770c-687e-43c8-a1e3-f5a4ff64c105