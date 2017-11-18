commit 92c06cbad591b88e69f416180052a72af455fa30
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Tue Sep 19 11:07:32 2017 -0700

    MINOR: Protocol schema refactor follow-up

    - Use constants in a few places that were missed
    - Remove ProtoUtils by moving its methods to Schema
    - Merge SchemaVisitor and SchemaVisitorAdapter
    - Change SchemaVisitor package.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jason Gustafson <jason@confluent.io>

    Closes #3895 from ijuma/protocol-schema-refactor-follow-ups