commit cb067ee00c17c8b66da854436e8e02f221fb3e53
Author: Andy Wilkinson <awilkinson@pivotal.io>
Date:   Wed Mar 18 16:16:58 2015 +0000

    Align expectations with Spring Data REST’s new behaviour in Fowler

    The response produced by Spring Data REST to a search that will always
    return a single result, i.e. the return type is Foo rather than
    List<Foo> or Page<Foo> has improved in Fowler. Previously, the response
    would contain a single embedded resource. In Fowler, the response now
    contains the resource that used to be embedded as a top-level resource.
    This commit updates the expectations in one of the sample’s tests to
    match this new behaviour.

    See gh-2673