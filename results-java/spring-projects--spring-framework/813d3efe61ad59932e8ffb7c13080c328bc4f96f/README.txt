commit 813d3efe61ad59932e8ffb7c13080c328bc4f96f
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Thu Mar 9 17:33:18 2017 -0500

    ExchangeResult refactoring in WebTestClient

    The WebTestClient API no longer provides access to a base
    ExchangeResult without a decoded response body.

    Instead the response has to be decoded first and tests can then
    access the EntityExchangeResult and FluxExchangeResult sub-types.