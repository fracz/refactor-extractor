commit 590f2235d99488721f44ea880682efaa01f2c70d
Author: Alan Viverette <alanv@google.com>
Date:   Thu Nov 12 12:43:15 2015 -0500

    Prevent runtime restart on missing magnification spec accessors

    The spec property requires an accessible getter and setter. This code
    will be removed in a following CL that refactors magnification spec
    animation.

    Change-Id: Ia8fecf98700d18e62ae30aa437b81b061c9a9542