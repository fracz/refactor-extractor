commit d13a4725f16636aed289cd084b9bfff92e9237b8
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Tue Dec 9 13:24:48 2014 +0300

    First implementation for hg graft command added; Cherry pick for git and graft for mercurial unified

    * cherry pick extension point added for appropriate action executors;
    * icons and common action moved to dvcs;
    * grafting state added to Repository states;
    * mercurial state dependant actions refactored;
    * annotations added