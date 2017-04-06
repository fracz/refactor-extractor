commit d39da1d06ddebca55caeee8d7439381eba8ba2ed
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Wed Mar 15 16:57:40 2017 -0400

    Polish + minor HttpHandler refactoring

    CompositeHttpHandler is public and called ContextPathCompositeHandler.

    Also an overhaul of the Javadoc on HttpHandler, WebHttpHandlerAdapter,
    and ContextPathCompositeHandler.