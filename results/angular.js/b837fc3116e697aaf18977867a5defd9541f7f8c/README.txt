commit b837fc3116e697aaf18977867a5defd9541f7f8c
Author: Jason Bedard <jason+github@jbedard.ca>
Date:   Thu Nov 5 22:30:55 2015 -0800

    refactor($compile): simplify multi element directive check

    Previously, we would check if an attribute indicates a multi-element
    directive, now we only do this check if the attribute name actually
    matches the multi-element name pattern.

    Closes #12365