commit 21218cca44bea26b7accf336f4a93e4c3aba0477
Author: raul782 <raulrodriguez782@gmail.com>
Date:   Fri Jul 5 03:06:42 2013 -0500

    [Serializer] Added XML attributes support for DomDocument in XmlEncoder.

    This is a combination of 2 commits.

    - [Serializer] Added encoding support for DomDocument in XmlEncoder

    - [Serializer] Refactor code to allow setting <?xml standalone ?>

    This commit refactors the createDomDocument(..) method in XmlEncoder
    so
    it can set the 'version', 'encoding' and 'standalone' attributes on
    the
    DOM document.

    Code coverage of new code: 100%. Tests: pass.