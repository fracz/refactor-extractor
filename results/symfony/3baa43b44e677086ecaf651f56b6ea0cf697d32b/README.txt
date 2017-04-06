commit 3baa43b44e677086ecaf651f56b6ea0cf697d32b
Merge: 9e13cc0 ab42e9c
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 26 12:51:10 2014 +0100

    Merge branch '2.4'

    * 2.4: (52 commits)
      Fix #8205 : Deprecate file mode update when calling dumpFile
      Fix #10437: Catch exceptions when reloading a no-cache request
      Fix libxml_use_internal_errors and libxml_disable_entity_loader usage
      removed ini check to make uploadedfile work on gae
      Update OptionsResolver.php
      fixed comment in forms.xml file
      Clean KernelInterface docblocks
      Cast the group name as a string
      Fixed doc of InitAclCommand
      [Form] Fix "Array was modified outside object" in ResizeFormListener.
      Fix IBAN validator
      [Process] Remove unreachable code + avoid skipping tests in sigchild environment
      Fixed bug that incorrectly causes the "required" attribute to be omitted from select even though it contains the "multiple" attribute
      Added travis_retry to .travis.yml
      [Process] fix some typos and refactor some code
      [Process] Fix unit tests in sigchild disabled environment
      [Process] Trow exceptions in case a Process method is supposed to be called after termination
      fixed typo
      [Process] fixed fatal errors in getOutput and getErrorOutput when process was not started
      [Process] Fix escaping on Windows
      ...

    Conflicts:
            src/Symfony/Bundle/FrameworkBundle/Command/ServerRunCommand.php
            src/Symfony/Component/Form/Extension/Core/EventListener/ResizeFormListener.php
            src/Symfony/Component/Process/Process.php
            src/Symfony/Component/Process/ProcessPipes.php
            src/Symfony/Component/Process/Tests/AbstractProcessTest.php