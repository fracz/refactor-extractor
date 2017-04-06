commit 7fc30463e2b8de71fccc5a445ab55b00e698d6b3
Merge: ddc508b 2c2daf1
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Wed Dec 30 11:33:57 2015 +0100

    Merge branch '2.7' into 2.8

    * 2.7:
      [Form] fix Catchable Fatal Error if choices is not an array
      [Process] Fix a transient test
      [Process] Fix potential race condition leading to transient tests
      [Routing] removed unused variable in PhpMatcherDumperTest class.
      [travis] use github token to fetch deps from ZIP files
      [DependencyInjection] fixes typo in triggered deprecation notice.
      [Form] improve deprecation messages for the "empty_value" and "choice_list" options in the ChoiceType class.

    Conflicts:
            src/Symfony/Component/Form/Extension/Core/Type/ChoiceType.php