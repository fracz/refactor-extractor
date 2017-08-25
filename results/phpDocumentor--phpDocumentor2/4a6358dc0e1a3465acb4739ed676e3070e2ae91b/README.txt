commit 4a6358dc0e1a3465acb4739ed676e3070e2ae91b
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Thu Feb 21 06:21:55 2013 +0100

    Make Twig writer fully functional

    The Twig writer was not capable of querying the Objetc Graph and correctly creating
    artifacts. With this refactoring we introduced a Object Graph Walker that is capable of interpreting
    a light version of the twig syntax to get the correct element from an object graph.