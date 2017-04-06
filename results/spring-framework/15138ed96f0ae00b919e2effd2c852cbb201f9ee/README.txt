commit 15138ed96f0ae00b919e2effd2c852cbb201f9ee
Author: Sebastien Deleuze <sdeleuze@pivotal.io>
Date:   Tue Apr 26 14:40:39 2016 +0200

    Revisit ScriptTemplateView resource loading

    Resources are now retrieved using the application context in order to
    support natively non-classpath locations like /WEB-INF/...

    As a consequence of this refactoring, ScriptTemplateView#createClassLoader()
    protected method as been removed, since it did not make sense anymore with
    this new resource loading implementation.

    Issue: SPR-14210