commit 4892436efe462a5836de2b039d2f18dda390bc6b
Author: Brian Clozel <bclozel@pivotal.io>
Date:   Fri Jul 8 11:40:12 2016 +0200

    Refactor HTTP client contracts

    This commit refactors the `ClientHttpRequestFactory` into an
    `ClientHttpConnector` abstraction, in order to reflect that
    `ClientHttpRequest`s only "exist" once the client is connected
    to the origin server.

    This is why the HTTP client is now callback-based, containing all
    interactions with the request within a
    `Function<ClientHttpRequest,Mono<Void>>` that signals when it's done
    writing to the request.

    The `ClientHttpRequest` contract also adopts `setComplete()`
    and promotes that method to the `ReactiveHttpOutputMessage` contract.

    This commit also adapts all other APIs to that change and fixes a few
    issues, including:

    * use `HttpMessageConverter`s instead of `Encoders`/`Decoders`
    * better handle type information about request content publishers
    * support client cookies in HTTP requests
    * temporarily remove the RxNetty client support