commit d31bf634da7cee8270996438dab13748bda7b464
Merge: c51f3f3 c1b1b10
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 3 11:12:11 2014 +0200

    Merge branch '2.5'

    * 2.5: (23 commits)
      [HttpKernel] fixed some unit tests for 2.4 (signature now uses SHA256 instead of MD5)
      [HttpKernel] simplified code
      [HttpKernel] fixed internal fragment handling
      fixing yaml indentation
      Unexpexted ));"
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
      ...

    Conflicts:
            src/Symfony/Bundle/WebProfilerBundle/Resources/views/Profiler/admin.html.twig
            src/Symfony/Component/Filesystem/Filesystem.php
            src/Symfony/Component/HttpKernel/Fragment/EsiFragmentRenderer.php