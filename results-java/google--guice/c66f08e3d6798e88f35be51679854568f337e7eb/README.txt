commit c66f08e3d6798e88f35be51679854568f337e7eb
Author: Sam Berlin <sameb@google.com>
Date:   Wed Jul 9 20:29:11 2014 -0400

    Automated g4 rollback of changelist 70738452.

    *** Reason for rollback ***

    Fix projects that were doing dependency analysis by returning a dependency on the Injector when run on raw Elements (instead of a dependency on @Actual+@Default, even though we didn't know which would really exist).

    *** Original change description ***

    Automated g4 rollback of changelist 70734332.

    *** Reason for rollback ***

    Broke a project.

    *** Original change description ***

    Simplify OptionalBinder implementation to not delegate to a MapBinder.
    Instead, bind directly to annotated @Actual/@Default keys.  This is in
    preparation for future improvements.

    This contains two logical changes:
    1) The error message for duplicate actual/default bindings is now the default Guice message, instead of a custom thing in MapBinder.
    2) getDependencies now...

    ***
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=70742247