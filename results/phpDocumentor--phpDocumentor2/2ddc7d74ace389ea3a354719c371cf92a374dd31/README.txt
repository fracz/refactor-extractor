commit 2ddc7d74ace389ea3a354719c371cf92a374dd31
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Thu Apr 4 19:25:28 2013 +0200

    Add AstXml writer

    Previously phpDocumentor2 generated an XML file that was interpreted by the XSL based
    templates to generate the HTML representation. With the current refactoring this has disappeared
    and a new writer is introduced to still generate that XML for those who want it