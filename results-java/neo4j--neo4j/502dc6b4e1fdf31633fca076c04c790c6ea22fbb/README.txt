commit 502dc6b4e1fdf31633fca076c04c790c6ea22fbb
Author: Jacob Hansson <jacob@voltvoodoo.com>
Date:   Mon Jun 4 14:13:15 2012 +0200

    Cleanup of the Neo4j Server, preparing for configuration and logging refactoring.

    This commit:

     * Deprecates specialized methods on WebServer (such as addSecurityFilter and addRequestLimitFilter)
       and replaces them with a single addFilter method. This simplifies for implementors of WebServer.

     * Removes all uses of getJetty(), resealing the abstraction WebServer was meant to provide,
       this prepares the way for other WebServer implementations.

     * Changes the method of generating specialized NeoServer types (Community, Advanced, Enterprise, Wrapping)
       from using factory methods sent into the constructor to using subclassing.

     * Clarifies the distinction between NeoServer and Bootstrapper. NeoServer acts as an assembly class,
       bootstrapper instantiates and starts/stops NeoServer. This change is reflected by the introduction
       of AbstractNeoServer, and the replacement of NeoServerWithEmbeddedWebServer with CommunityNeoServer.

     * Modifies the ServerModule interface such that the interface moves closer to the LifeCycle interface,
       to lead the way for ServerModule eventually extending that.