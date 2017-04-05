commit ab771dfd97b14f978a0513f4e9425db86099b348
Author: Sam Brannen <sam@sambrannen.com>
Date:   Fri Mar 20 17:11:52 2015 +0100

    Refactor tests to use the new database-name attribute

    This commit refactors the XML configuration used by the tests in the
    Spr8849Tests test suite so that a unique database name is always
    generated (via the new 'database-name' attribute that was introduced in
    SPR-12835) while reusing the same bean name (i.e., 'dataSource').

    This is a much more robust alternative to the previous work-around
    since the name of the DataSource does not randomly change across
    application contexts, thus allowing proper autowiring by name and bean
    referencing within XML configuration.

    Issue: SPR-8849