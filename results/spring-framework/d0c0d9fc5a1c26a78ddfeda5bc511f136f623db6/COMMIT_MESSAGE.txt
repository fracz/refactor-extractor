commit d0c0d9fc5a1c26a78ddfeda5bc511f136f623db6
Author: Sam Brannen <sam@sambrannen.com>
Date:   Sat Jun 20 18:27:36 2015 +0200

    Synthesize annotation from defaults

    This commit introduces a convenience method in AnnotationUtils for
    synthesizing an annotation from its default attribute values.

    TransactionalTestExecutionListener has been refactored to invoke this
    new convenience method.

    Issue: SPR-13087