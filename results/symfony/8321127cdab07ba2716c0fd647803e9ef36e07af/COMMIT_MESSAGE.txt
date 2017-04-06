commit 8321127cdab07ba2716c0fd647803e9ef36e07af
Merge: 1949ce0 835c1b8
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jan 8 19:16:44 2013 +0100

    Merge branch '2.0' into 2.1

    * 2.0:
      [Bundle] [FrameworkBundle] fixed indentation in esi.xml services file.
      [TwigBundle] There is no CSS visibility of display, should be visible instead
      [DependencyInjection] fixed a bug where the strict flag on references were lost (closes #6607)
      [HttpFoundation] Check if required shell functions for `FileBinaryMimeTypeGuesser` are not disabled
      [CssSelector] added css selector with empty string
      [HttpFoundation] Docblock for Request::isXmlHttpRequest() now points to Wikipedia
      [DependencyInjection] refactored code to avoid logic duplication

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Resources/config/esi.xml
            src/Symfony/Component/DependencyInjection/Dumper/PhpDumper.php
            src/Symfony/Component/HttpFoundation/File/MimeType/FileBinaryMimeTypeGuesser.php