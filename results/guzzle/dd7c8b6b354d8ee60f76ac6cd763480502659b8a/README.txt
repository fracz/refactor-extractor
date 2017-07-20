commit dd7c8b6b354d8ee60f76ac6cd763480502659b8a
Author: Michael Dowling <mtdowling@gmail.com>
Date:   Sun May 6 19:08:27 2012 -0700

    Lots of refactoring for *greatly* improved performance (4-5x faster)

    [Http] cache.* parameters should be prefixed with 'params.' now.

    Adding the ability to set arbitrary curl options that don't map to constants (disable_wire, progress, etc)

    Added a check to the CurlMulti object that prevents sending a request when the curl multi resource has been destroyed.  This could possibly happen when issuing requests from a destructor.

    Adding the ability to disable type validation of configuration options

    Adding a cache for all requests owned by a curl multi object

    Creating the polling event external to the main CurlMulti loop

    Hardening the CurlMulti class so that curl handles that are finished sending are removed from the multi handle.  Adding validation test

    [Common] BC: Simplifying Guzzle\Common\Collection so that it is more performant.

    [Service] Adding the ability to disable validation on the Inpector class.  Disabling validation on first instantiating commands so that defaults are added but nothing is check until attempting to execute a command.

    Using class properties instead of a Collection for ApiCommand

    BC: Using a custom validation system that allows a flyweight
    implementation for much faster validation. No longer using Symfony2
    Validation component.