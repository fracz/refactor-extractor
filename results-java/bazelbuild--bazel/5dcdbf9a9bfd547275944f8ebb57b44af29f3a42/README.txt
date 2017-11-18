commit 5dcdbf9a9bfd547275944f8ebb57b44af29f3a42
Author: Francois-Rene Rideau <tunes@google.com>
Date:   Thu Feb 19 18:36:17 2015 +0000

    Introduce first class function signatures; make the parser use them.
    This is the first meaty cl in a series to refactor the Skylark function call protocol.

    1- We introduce a first-class notion of FunctionSignature, that supports positional and named-only arguments, mandatory and optional, default values, type-checking, *stararg and **kwarg;

    2- To keep things clean, we distinguish two different kinds of Argument's: Argument.Passed that appears in function calls, and Parameter, that appears in function definitions.

    3- We refactor the Parser so it uses this infrastructure, and make minimal changes to MixedModeFunction so that it works with it (but don't actually implement *starparam and **kwparam yet).

    4- As we modify FuncallExpression, we ensure that the args and kwargs arguments it passes to the underlying function are immutable, as a prerequisite to upcoming implementation of *starparam and **kwparam as being provided directly from a skylark list or dict.

    Further changes under review will take advantage of this FunctionSignature to redo all our function call protocol, to be used uniformly for both UserDefinedFunction's and builtin function. The result will be a simpler inheritance model, with better type-checking, builtin functions that are both simpler and better documented, and many redundant competing functionality-limited codepaths being merged and replaced by something better.

    NB: The changes to MixedModeFunction, SkylarkFunction and MethodLibrary are temporary hacks to be done away with in an upcoming CL. The rest is the actual changes.

    --
    MOS_MIGRATED_REVID=86704072