commit 9ab2c6628b4f912b8ec23cd8dc3ccd3e54b5fa8e
Author: Chris Beams <cbeams@vmware.com>
Date:   Wed Aug 18 10:37:35 2010 +0000

    Split IoC chapter DocBook XML into multiple files (SPR-7467)

    All <section/> elements in beans.xml >=~ 500 lines have been broken out
    into separate documents with DOCTYPE 'section'. This refactoring makes
    working with these files much easier in wysiwyg editors (namely
    oXygen Author).

    For consistency, this same refactoring should be applied to all other
    chapters much larger than 1500 lines, such as aop.xml (3861), mvc.xml
    (3466), jdbc.xml (3042), and so on.

    beans.xml and the new section files have also been formatted for
    consistency and to avoid whitespace diffs as much as possible into the
    future.