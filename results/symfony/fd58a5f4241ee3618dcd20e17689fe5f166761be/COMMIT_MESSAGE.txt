commit fd58a5f4241ee3618dcd20e17689fe5f166761be
Merge: 4b967b1 41fef93
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Apr 17 07:27:14 2013 +0200

    Merge branch '2.1' into 2.2

    * 2.1:
      Fix default value handling for multi-value options
      [HttpKernel] truncate profiler token to 6 chars (see #7665)
      Disabled APC on Travis for PHP 5.5+ as it is not available
      [HttpFoundation] do not use server variable PATH_INFO because it is already decoded and thus symfony is fragile to double encoding of the path
      [Yaml] improved boolean naming ($notEOF -> !$EOF)
      [Yaml] fixed handling an empty value
      [Routing][XML Loader] Add a possibility to set a default value to null
      The /e modifier for preg_replace() is deprecated in PHP 5.5; replace with preg_replace_callback()
      [HttpFoundation] Fixed bug in key searching for NamespacedAttributeBag
      [Form] DateTimeToRfc3339Transformer use proper transformation exteption in reverse transformation
      Update PhpEngine.php
      [HttpFoundation] getClientIp is fixed.

    Conflicts:
            .travis.yml
            src/Symfony/Component/Routing/Loader/XmlFileLoader.php
            src/Symfony/Component/Routing/Loader/schema/routing/routing-1.0.xsd
            src/Symfony/Component/Routing/Tests/Fixtures/validpattern.xml
            src/Symfony/Component/Routing/Tests/Loader/XmlFileLoaderTest.php