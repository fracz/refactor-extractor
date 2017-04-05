commit e1720d89fcf65fca6b244df1696c1df67fc0c808
Author: Sam Brannen <sam@sambrannen.com>
Date:   Mon Apr 21 12:46:38 2014 -0400

    Don't mutate annotation metadata when merging attrs

    Prior to this commit, invoking the getMergedAnnotationAttributes()
    method in AnnotationReadingVisitorUtils resulted in mutation of the
    internal state of the ASM-based annotation metadata supplied to the
    method.

    This commit fixes this issue by making a copy of the original
    AnnotationAttributes for the target annotation before merging attribute
    values from the meta-annotation hierarchy.

    This commit also introduces a slight performance improvement by
    avoiding duplicate processing of the attributes of the target
    annotation.

    Issue: SPR-11710