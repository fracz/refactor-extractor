commit 60bbb8f38079bffa77a765d663658de853208ebb
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Nov 23 22:43:09 2010 +0100

    [DependencyInjection] optimized compiled containers

     * removed the __call() method in Container: it means that now, there is only
       one way to get a service: via the get() method;

     * removed the $shared variable in the dumped Container classes (we now use
       the $services variable from the parent class directly -- this is where we
       have a performance improvement);

     * optimized the PHP Dumper output.