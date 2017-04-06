commit ab42e9cbc4078493e15cddff4e45539f48f672db
Merge: bdddf3a ca5eea5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 26 12:35:33 2014 +0100

    Merge branch '2.3' into 2.4

    * 2.3: (34 commits)
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
            src/Symfony/Component/DomCrawler/Crawler.php
            src/Symfony/Component/Filesystem/Filesystem.php
            src/Symfony/Component/Process/Process.php