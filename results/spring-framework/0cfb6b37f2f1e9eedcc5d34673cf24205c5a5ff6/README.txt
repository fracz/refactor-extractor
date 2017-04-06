commit 0cfb6b37f2f1e9eedcc5d34673cf24205c5a5ff6
Author: Arjen Poutsma <apoutsma@pivotal.io>
Date:   Thu Oct 20 12:13:07 2016 +0200

    Align Reactive WebClient with web.reactive.function

    This commit refactors the web client to be more similar to
    web.reactive.function. Changes include:

    - Refactor ClientWebRequest to immutable ClientRequest with builder and
       support for BodyInserters.
    - Introduce ClientResponse which exposes headers, status, and support
       for reading from the body with BodyExtractors.
    - Removed ResponseErrorHandler, in favor of having a ClientResponse
       with "error" status code (i.e. 4xx or 5xx). Also removed
       WebClientException and subclasses.
    - Refactored WebClientConfig to WebClientStrategies.
    - Refactored ClientHttpRequestInterceptor to ExchangeFilterFunction.
    - Removed ClientWebRequestPostProcessor in favor of
       ExchangeFilterFunction, which allows for asynchronous execution.

    Issue: SPR-14827