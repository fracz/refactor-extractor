commit 8358ebb15ab44fb0cc40401ee9721c19de7a4f8c
Merge: 8fdfb6f 23eb033
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Sep 3 10:42:07 2014 +0200

    Merge branch '2.3' into 2.4

    * 2.3:
      [HttpKernel] fixed internal fragment handling
      fixing yaml indentation
      [WebProfiler] replaced the import/export feature from the web interface to a CLI tool
      Forced all fragment uris to be signed, even for ESI
      Add tests and more assertions
      [FrameworkBundle][Translator] Validate locales.
      [HttpFoundation] added some missing tests
      [HttpFoundation] Improve string values in test codes
      fix comment: not fourth but sixth argument
      fixing typo in a comment
      [FrameworkBundle] fixed CS
      [FrameworkBundle] PhpExtractor bugfix and improvements
      [Finder] Fix findertest readability
      [Filesystem] Add FTP stream wrapper context option to enable overwrite (override)
      fix parsing of Authorization header
      Test examples from Drupal SA-CORE-2014-003
      Fix potential DoS when parsing HOST
      Made optimization deprecating modulus operator

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Resources/config/esi.xml
            src/Symfony/Component/HttpFoundation/Request.php
            src/Symfony/Component/HttpFoundation/Tests/RequestTest.php
            src/Symfony/Component/HttpKernel/Fragment/EsiFragmentRenderer.php