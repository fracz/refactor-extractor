commit 756b39b907ea5261358361a2fce18ae76c5a3596
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Mar 30 16:18:26 2012 +0000

    Fix by Sorel Johann refactoring actual index factory to support 3rd party index factories. Below his comments:

    This patch focuses on automatic index type discovery.
    To achieve this I had to review the OIndexFactory class which did not allow such discovery mechanism.

    I invite you to explore the OIndexes class and rely on similar patterns whenever you need plugin points in the project.
    In this case I used the ServiceRegistry but ServiceLoader would work too (both are standard jre classes).
    The only main difference is that the first one has the mechanic to order the factories, might not be useful now but perhaps in the future.

    The javadoc in OIndexes detail how new OIndexFactory can be registered.
    Basically any jar in the class path which declare a OIndexFactory using META-INF/services will be automatically seen.