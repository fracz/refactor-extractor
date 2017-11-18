commit 9e7a01abe299762cc5369cd125679178ff1f1bde
Author: daz <darrell.deboer@gradleware.com>
Date:   Thu Feb 20 11:36:55 2014 -0700

    REVIEW-3914: Refactoring and improvements to native incremental compile
    - Renamed Includes -> SourceIncludes, IncludesParser -> SourceIncludesParser
    - Replaced SourceDependencyParser with SourceIncludesResolver
        - Does not wrap SourceIncludesParser, but simply resolves a SourceIncludes instance to a set of files
    - For incremental compile, persists SourceIncludes for source file as well as results of resolving those includes
        - Detects changes in include path for compilation