commit 9040306545ddd134a8b0ebe97a21b581cc3df962
Author: Sam Berlin <sameb@google.com>
Date:   Wed Jul 9 20:28:00 2014 -0400

    Simplify OptionalBinder implementation to not delegate to a MapBinder.
    Instead, bind directly to annotated @Actual/@Default keys.  This is in
    preparation for future improvements.

    This contains two logical changes:
    1) The error message for duplicate actual/default bindings is now the default Guice message, instead of a custom thing in MapBinder.
    2) getDependencies now returns the live dependency (either the actual or the default, not both).  Also, Optional<Provider<T>> continues to return a dep to Provider<T>, but Optional<T> returns a dep of T directly.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=70734332