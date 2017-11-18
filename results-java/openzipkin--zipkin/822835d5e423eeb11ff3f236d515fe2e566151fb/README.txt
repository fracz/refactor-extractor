commit 822835d5e423eeb11ff3f236d515fe2e566151fb
Author: sethp-jive <seth.pellegrino@jivesoftware.com>
Date:   Fri Sep 16 18:28:15 2016 -0700

    storage: introduce elasticsearch client shim (#1303)

    In order to support elasticsearch on AWS (and other *aaS vendors) that
    choose not to expose the ES binary protocol, we must have an option for
    clients to use the native transport or REST.

    Towards that end, this change refactors the existing storage client
    interactions behind an interface that can be replaced by a different
    transport. Almost all the changes were non-functional, with the
    exception of three methods:

    * getRawTrace
    * getServiceNames
    * getSpanNames

    Which now use the leinientExpandOpen strategy for their index selection.
    We anticipate this is a positive change, as skipping unavailable (or
    closed) indexes will at worst present a partial span for the period
    of time that the index is unavailable, which seems be prefereable to
    a hard error.

    Also, when using the same elasticsearch cluster for multiple Zipkin
    instances (i.e. running tests + a local development server) the
    instances will race to claim the `zipkin_template` with a pattern
    that will match only their indices – so if the tests are run first,
    then the local server's indices will have only an implicit schema, or
     vice versa.

    This change modifies the name of the template on a per-index-pattern
    basis, so multiple templates can happily coexist. For the default
    `zipkin` index prefix, users should not observe any change in
    behavior. NB: It may be necessary to delete unstructured indexes or
    clear out old `zipkin_template` templates in some cases to get
    multiple instances working together.

    This commit is the first step in support of #1302