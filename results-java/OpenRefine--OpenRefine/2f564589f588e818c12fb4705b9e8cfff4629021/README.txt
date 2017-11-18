commit 2f564589f588e818c12fb4705b9e8cfff4629021
Author: Iain Sproat <iainsproat@gmail.com>
Date:   Thu Nov 11 13:15:41 2010 +0000

    Adding a Fixed Width data importer (Issue 85) and associated tests.

    Although this importer is 'wired up', it requires a property "fixed-column-widths" which is not (yet) implemented in the UI.  But the ImporterRegister.guessImporter method will probably select the CsvTsvImporter before the FixedWidthImporter anyway.  I suggest an improvement to the project creation UI and/or the guessImporter method will be required.

    git-svn-id: http://google-refine.googlecode.com/svn/trunk@1857 7d457c2a-affb-35e4-300a-418c747d4874