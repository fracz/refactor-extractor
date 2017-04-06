commit dda085e8c4e86934452cd515acf0849fa28e08b4
Merge: 23919ab 3c10715
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Sat Aug 1 08:48:35 2015 +0200

    Merge branch '2.8'

    * 2.8: (63 commits)
      [Debug] Deprecate ExceptionHandler::createResponse
      [Debug] cleanup ExceptionHandlerTest
      Reordered the toolbar elements via service priorities
      bumped Symfony version to 2.7.4
      Increased the z-index of .sf-toolbar-info
      Removed an unused media query
      updated VERSION for 2.7.3
      updated CHANGELOG for 2.7.3
      Redesigned "abbr" elements
      Restored the old behavior for toolbars with lots of elements
      Tweaks and bug fixes
      Added some upgrade notes about the new toolbar design
      fixed typo in translation keys
      Fix the return value on error for intl methods returning arrays
      Removed an useless CSS class and added styles for <hr>
      Added a new profiler_markup_version to improve BC of the new toolbar
      Fix merge
      Removed an unused import
      Reverted the feature to display different toolbar versions
      Minor JavaScript optimizations
      ...

    Conflicts:
            CHANGELOG-2.7.md
            UPGRADE-2.8.md
            src/Symfony/Bundle/FrameworkBundle/Resources/config/collectors.xml
            src/Symfony/Component/Debug/composer.json
            src/Symfony/Component/HttpKernel/HttpCache/HttpCache.php