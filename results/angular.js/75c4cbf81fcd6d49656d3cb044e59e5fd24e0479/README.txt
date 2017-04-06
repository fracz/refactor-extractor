commit 75c4cbf81fcd6d49656d3cb044e59e5fd24e0479
Author: Tobias Bosch <tbosch1009@gmail.com>
Date:   Fri Aug 22 11:43:58 2014 -0700

    refactor($compile): rename `directive.type` to `directive.templateNamespace`

    Also corrects the tests for MathML that use `directive.templateNamespace`.

    BREAKING CHANGE (within 1.3.0-beta): `directive.type` was renamed to `directive.templateNamespace`

    The property name `type` was too general.