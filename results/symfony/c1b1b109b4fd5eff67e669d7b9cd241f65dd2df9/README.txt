commit c1b1b109b4fd5eff67e669d7b9cd241f65dd2df9
Merge: 36b0e72 4ef1328
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 3 11:00:14 2014 +0200

    Merge branch '2.4' into 2.5

    * 2.4: (21 commits)
      [HttpKernel] fixed some unit tests for 2.4 (signature now uses SHA256 instead of MD5)
      [HttpKernel] simplified code
      [HttpKernel] fixed internal fragment handling
      fixing yaml indentation
      [WebProfiler] replaced the import/export feature from the web interface to a CLI tool
      Forced all fragment uris to be signed, even for ESI
      Add tests and more assertions
      [FrameworkBundle][Translator] Validate locales.
      [HttpFoundation] added some missing tests
      [HttpFoundation] Improve string values in test codes
      [Security] Add more tests for StringUtils::equals
      fix comment: not fourth but sixth argument
      fixing typo in a comment
      [FrameworkBundle] fixed CS
      [FrameworkBundle] PhpExtractor bugfix and improvements
      [Finder] Fix findertest readability
      [Filesystem] Add FTP stream wrapper context option to enable overwrite (override)
      fix parsing of Authorization header
      Test examples from Drupal SA-CORE-2014-003
      Fix potential DoS when parsing HOST
      ...

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Tests/Translation/TranslatorTest.php
            src/Symfony/Bundle/FrameworkBundle/Translation/Translator.php