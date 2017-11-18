commit d71c56383147f36af5e6f9592abfba18aff7fbfb
Author: Iain Sproat <iainsproat@gmail.com>
Date:   Mon Sep 27 17:40:51 2010 +0000

    XmlImportUtilities.detectPathFromTag and XmlImportUtilities.detectRecordElement methods now use a generic TreeParser interface.  A lightweight wrapper XmlParser wraps XMLStreamReader to provide parsing for xml data.

    This is another small step towards a generic importer for tree structured data.  My plan is to refactor more of XmlImportUtilities' methods to use the TreeParser interface so that XmlStreamReader is no longer called directly from XmlImportUtilities.

    git-svn-id: http://google-refine.googlecode.com/svn/trunk@1322 7d457c2a-affb-35e4-300a-418c747d4874