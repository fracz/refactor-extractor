commit 14cecd5231f69b62e544eb7f20f12ecb68de5e92
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 17 17:18:30 2010 +0200

    [Routing] refactored loaders

     * refactored the import mechanism for better flexibility
     * added two methods to LoaderInterface: supports() and setResolver()
     * added a LoaderResolver interface
     * added a Loader base class
     * added new loaders: DelegatingLoader, ClosureLoader, and PhpFileLoader
     * changed the Router constructor signature (now takes a Loader)