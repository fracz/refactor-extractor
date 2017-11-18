commit b6011294b1c39911ce8a31303a50633a34638f6f
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Fri May 31 20:04:57 2013 +0400

    Git and Mercurial repository refactoring

    *common classes for Git and Mercurial repository extracted;
    *unnecessary classes removed;
    *common static methods moved to appropriate Util class;
    *annotations added;
    *fully-qualified names removed from javadoc;
    *field for storing fresh state added to HgRepositoryImpl;
    *additional checks is repository fresh added for mercurial repository reader;