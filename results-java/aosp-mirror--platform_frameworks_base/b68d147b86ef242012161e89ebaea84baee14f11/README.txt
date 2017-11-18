commit b68d147b86ef242012161e89ebaea84baee14f11
Author: Raph Levien <raph@google.com>
Date:   Wed Feb 10 16:56:26 2016 -0800

    Allow null string to BidiFormatter#unicodeWrap

    The BidiFormatter#unicodeWrap() methods allow null string, but the
    toString() call on the result in the refactored code was crashing.
    Also adds @Nullable annotations.

    Bug: 27124532
    Change-Id: I25922d104587af4a850c3640987af9315c3d3bde