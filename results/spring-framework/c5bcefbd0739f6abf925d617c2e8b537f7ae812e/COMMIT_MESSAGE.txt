commit c5bcefbd0739f6abf925d617c2e8b537f7ae812e
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Tue Mar 28 17:50:36 2017 -0400

    Complete RequestMappingHandlerAdapter refactoring

    ControllerMethodResolver now also encapsulates initialization, storage,
    and use of HandlerMethodArgumentResolver's by annotated method type.