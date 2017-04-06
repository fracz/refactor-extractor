commit 59e43269890f724f5d3284a08bade5a61ee55505
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Thu Oct 13 16:59:33 2016 +0200

    Various improvements to web.reactive.function

    This commit introduces the following changes to web.reactive.function:

    - Added RouterFunction.andRoute(), a combination of RouterFunction.and()
    with RouterFunctions.route()
    - ServerRequest.pathVariable() returns String instead of
    Optional<String>. An exception is thrown if the variable is not present.
    - Added HandlerFilterFunction.andThen and HandlerFilterFunction.apply()