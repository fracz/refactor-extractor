commit 3928ed5fce9f4e1b2120598b3c612886662f7d1c
Merge: 7c53373 7f24883
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Apr 27 14:43:05 2015 +0200

    Merge branch '2.3' into 2.6

    * 2.3:
      [DependencyInjection] Removed extra strtolower calls
      [Validator] Fixed Choice when an empty array is used in the "choices" option
      Fixed tests
      [StringUtil] Fixed singularification of 'selfies'
      Fix Portuguese (Portugal) translation for Security
      improved exception when missing required component
      CS: unalign =
      Show a better error when the port is in use
      CS: unalign =>
      [FrameworkBundle] Check for 'xlf' instead of 'xliff'
      Add better phpdoc message for getListeners method of the EventDispatcher

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Command/ServerRunCommand.php
            src/Symfony/Bundle/TwigBundle/Command/LintCommand.php
            src/Symfony/Component/DependencyInjection/ContainerBuilder.php
            src/Symfony/Component/DependencyInjection/Tests/Fixtures/php/services11.php
            src/Symfony/Component/Validator/Constraints/ChoiceValidator.php