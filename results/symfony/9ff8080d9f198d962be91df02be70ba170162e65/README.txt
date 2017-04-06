commit 9ff8080d9f198d962be91df02be70ba170162e65
Merge: ea7a87a 11fb845
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Apr 5 08:16:53 2011 +0200

    Merge remote branch 'danielholmes/phpunit_xml_dir'

    * danielholmes/phpunit_xml_dir:
      [FrameworkBundle] removed function for checking for phpunit.xml in cwd
      [FrameworkBundle] fixed error with arg reversing from previous changes
      [FrameworkBundle] refactored getPhpUnitXmlDir in to separate parts and added support for --confguration=... style phpunit flag
      [FrameworkBundle] fixed getPhpUnitXmlDir to work with directory paths with spaces in them