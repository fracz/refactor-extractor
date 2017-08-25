commit e4e5f4d8b0eaea4cf9e33efcb7145f710eeb620d
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Wed Dec 7 21:13:58 2016 +0100

    Prevent fatal error on non-existent command class, see #861

    If a custom-command class can not be instantiated, magerun ran into a
    fatal error instantiating a non-existent class.

    Fix is to check for class existence first. Now if the class can not be
    instantiated, an error message is shown. If verbosity is high next to
    the error-message some more explanation is given.

    The fatal error was triggered because a home-dir module did "steal" the
    PSR-0 autoload prefix from a system module while not providing all the
    classes of the system module. As all custom-command-classes are merged,
    the system module's command classes were still instantiated w/o properly
    checking the user input (via configuration classes).

    Additionally command class-names are now verified to be subclasses of the
    Symfony command class. This is a precaution to make the use of magerun
    safer as it won't instantiate *any* classes via configuration files.

    Additionally polish of some code and improvement of verbose messages.

    Refs:

    - #861