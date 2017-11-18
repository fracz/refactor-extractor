commit a46125ca0e4569a41d2d078c3168dbfd8950534a
Author: Janak Ramakrishnan <janakr@google.com>
Date:   Wed Feb 11 16:51:37 2015 +0000

    Extract an EvaluableBlazeQueryEnvironment abstract class from BlazeQueryEnvironment that exposes an evaluateQuery method, and also implements the non-LabelVisitor-specific parts of BlazeQueryEnvironment, for other implementations' uses.

    Most of the code is just a straight refactoring of BlazeQueryEnvironment into EvaluableBlazeQueryEnvironment (and BlazeTargetAccessor). Ignoring whitespace changes in [] may be your friend for seeing that it's a straight move.

    This also allows us to change tests to use QueryCommand.newQueryEnvironment, in preparation for newQueryEnvironment potentially returning a different EvaluableBlazeQueryEnvironment subclass depending on the circumstances.

    --
    MOS_MIGRATED_REVID=86088953