commit 5ae41eb77073b47e75c2dcf3766137f2352fbaf9
Author: limpbizkit <limpbizkit@d779f126-a31b-0410-b53b-1d3aecad763e>
Date:   Sat Jun 6 17:51:27 2009 +0000

    Post release refactor. I moved almost everything that was package-private in com.google.inject to the internal package. The motivation is to simplify things - the previous setup was extremely awkward because c.g.i could call into internal, but not the reverse. Similarly for the SPI package.

    This refactoring reduces the visibility of many symbols in internal, and makes some other code hygeine changes.

    Once this is checked in, it should be much easier to add the toConstructor() binding type. That was previously difficult because the ConstructorBindingImpl class was package private and intertwined with that package.

    git-svn-id: https://google-guice.googlecode.com/svn/trunk@1002 d779f126-a31b-0410-b53b-1d3aecad763e