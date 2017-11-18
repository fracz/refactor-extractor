commit 529e62921d3aca4499689fcf7c7e2bbeb7ad221e
Author: Rossen Stoyanchev <rstoyanchev@vmware.com>
Date:   Tue Jul 24 16:00:05 2012 -0400

    Refactor Servlet 3 async support

    As a result of the refactoring, the AsyncContext dispatch mechanism is
    used much more centrally. Effectively every asynchronously processed
    request involves one initial (container) thread, a second thread to
    produce the handler return value asynchronously, and a third thread
    as a result of a dispatch back to the container to resume processing
    of the asynchronous resuilt.

    Other updates include the addition of a MockAsyncContext and support
    of related request method in the test packages of spring-web and
    spring-webmvc. Also an upgrade of a Jetty test dependency required
    to make tests pass.

    Issue: SPR-9433