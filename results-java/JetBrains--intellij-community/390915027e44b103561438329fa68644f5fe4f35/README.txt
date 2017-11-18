commit 390915027e44b103561438329fa68644f5fe4f35
Author: Vyacheslav Lukianov <lvo@jetbrains.com>
Date:   Tue May 2 19:20:12 2006 +0400

    refactored:
    - AntElement is PsiNamedElement
    - referece ids of structured elements are covered as anonymous AntElements, which references are resolved in
    - custom resolve if ref ids