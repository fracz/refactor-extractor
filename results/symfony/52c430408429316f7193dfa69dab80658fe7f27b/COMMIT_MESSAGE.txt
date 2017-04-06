commit 52c430408429316f7193dfa69dab80658fe7f27b
Merge: 3040977 7fc3046
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Dec 30 11:41:35 2015 +0100

    Merge branch '2.8' into 3.0

    * 2.8:
      [Form] fix Catchable Fatal Error if choices is not an array
      [Process] Fix a transient test
      [Process] Fix potential race condition leading to transient tests
      [DoctrineBridge] [PropertyInfo] Catch Doctrine\ORM\Mapping\MappingException
      [Routing] removed unused variable in PhpMatcherDumperTest class.
      [travis] use github token to fetch deps from ZIP files
      [DependencyInjection] fixes typo in triggered deprecation notice.
      [Form] improve deprecation messages for the "empty_value" and "choice_list" options in the ChoiceType class.
      [Form] Fixed regression on Collection type
      add missing symfony/polyfill-php55 dependency

    Conflicts:
            src/Symfony/Component/DependencyInjection/SimpleXMLElement.php
            src/Symfony/Component/Form/Extension/Core/Type/ChoiceType.php
            src/Symfony/Component/Form/Tests/Extension/Core/Type/ChoiceTypeTest.php
            src/Symfony/Component/Form/Tests/Extension/Core/Type/CollectionTypeTest.php
            src/Symfony/Component/HttpFoundation/composer.json