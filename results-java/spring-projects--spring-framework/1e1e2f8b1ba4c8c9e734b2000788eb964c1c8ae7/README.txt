commit 1e1e2f8b1ba4c8c9e734b2000788eb964c1c8ae7
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Thu Jul 7 04:16:04 2016 -0400

    Support HttpEntity method arguments

    The RequestBodyArgumentResolver has been refactored to have a shared
    base class and tests with the new HttpEntityMethodArgumentResolver.

    An HttpEntity argument is not expected to have an async wrapper because
    the request headers are available immediately. The body however can be
    asynchronous, e.g. HttpEntity<Flux<String>>.