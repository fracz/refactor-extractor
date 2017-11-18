commit 85294c2b6ecc3b1a05976e899e1996ed5e9909a0
Author: Adrian Cole <acole@pivotal.io>
Date:   Wed Jan 20 09:51:40 2016 +0800

    Reorganizes maven artifacts under io.zipkin.java, repackages as "zipkin"

    This reorganizes the maven project so that it is easier to add new
    features, such as a Cassandra SpanStore, without bumping into existing
    artifacts from the scala project. It does this by placing all artifacts
    under a package corresponding to the repository: io.zipkin.java.

    This also decouples the java package hierarchy from the domain name. By
    using the package "zipkin" as opposed to "io.zipkin", when we change the
    model in an incompatible way, say version 2, we can rename it to
    "zipkin2", where "io.zipkin2" makes less sense. This is the convention
    used in OkHttp and Retrofit. It is also similar to HTrace's approach,
    which also makes the major version a component of the package name.

    This also introduces some other naming conventions taken from Retrofit.

    For example, root directories that are not prefixed with zipkin are not
    published. For example, interop and benchmark modules are not published,
    while zipkin-spanstores are.

    As the repo is now namespaced properly via the project id, artifacts can
    have simpler names, for example "zipkin" instead of "zipkin-java-core".
    Moreover, we now don't mix artifacts in the same namespace as the scala
    ones. This would lead to less confusion about a spanstore-cassandra in
    the future.

    See http://jakewharton.com/java-interoperability-policy-for-major-version-updates/