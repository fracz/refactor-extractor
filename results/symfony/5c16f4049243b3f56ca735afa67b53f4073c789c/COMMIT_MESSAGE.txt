commit 5c16f4049243b3f56ca735afa67b53f4073c789c
Merge: 586c58a 52c4304
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Dec 30 11:41:51 2015 +0100

    Merge branch '3.0'

    * 3.0:
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