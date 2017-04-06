commit 4e3e86c4a70130cbe1e292005eddf49a4fb8d9a0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Jul 17 17:40:38 2010 +0200

    refactored routing management (it's now possible to disable the default routing)

     * removed the Kernel::registerRoutes() method
     * added a router entry in <web:config> (replaces the registerRoutes() method)
           <web:config>
               <web:router resource="%kernel.root_dir%/config/routing.xml" />
           </web:config>
     * refactored routing configuration in its own routing.xml file (leverages the new routing component API),
       which is loaded only if <web:router> is defined in the configuration