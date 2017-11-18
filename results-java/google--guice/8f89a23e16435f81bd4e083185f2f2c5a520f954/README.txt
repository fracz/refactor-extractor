commit 8f89a23e16435f81bd4e083185f2f2c5a520f954
Author: Sam Berlin <sameb@google.com>
Date:   Wed Jul 9 20:28:35 2014 -0400

    Automated g4 rollback of changelist 70734332.

    *** Reason for rollback ***

    Broke a project.

    *** Original change description ***

    Simplify OptionalBinder implementation to not delegate to a MapBinder.
    Instead, bind directly to annotated @Actual/@Default keys.  This is in
    preparation for future improvements.

    This contains two logical changes:
    1) The error message for duplicate actual/default bindings is now the default Guice message, instead of a custom thing in MapBinder.
    2) getDependencies now returns the live dependency (either the actual or the default, not both).  Also, Optional<Provider<T>> continues to return a de...

    ***
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=70738452