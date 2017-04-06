commit 60c6827f233a40a9cad618fc56ce5c2c3e2d6e53
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Jul 18 11:26:47 2010 +0200

    [DependencyInjection] refactored loaders

     * refactored the import mechanism for better flexibility
     * added two methods to LoaderInterface: supports() and setResolver()
     * added a LoaderResolver interface
     * added a Loader base class
     * added new loaders: DelegatingLoader, PhpFileLoader, and ClosureLoader