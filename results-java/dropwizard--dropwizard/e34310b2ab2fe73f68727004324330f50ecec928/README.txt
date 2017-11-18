commit e34310b2ab2fe73f68727004324330f50ecec928
Author: Tim Bartley <tbartley@anonyome.com>
Date:   Wed Aug 3 01:44:55 2016 +0200

    Allow use of custom HostnameVerifier on clients.

    While the improvements to TLS configuration of HTTP clients in 1.0.0
    (maybe prior) are awesome, as part of that process the ability to set a
    custom HostnameVerifier easily on the HTTP client has been lost.

    You used to be able to do e.g. as:

    JerseyClientConfiguration myJerseyClientConfiguration = <some
    configuration>;
    HostnameVerifier verifier = new MyCustomHostnameVerifier();
    JerseyClientBuilder clientBuilder = new JerseyClientBuilder(env);
    clientBuilder.using(myJerseyClientConfiguration).using(verifier);
    Client httpClient = clientBuilder.build();
    Same is true for HttpClientBuilder too.

    You can still do it by creating a custom Apache
    Registry<ConnectionSocketFactory> but you need to set up socket
    factories for every scheme.

    This change restores the ability to set a custom HostnameVerifier
    for clients.

    [Fixes #1663]
    (cherry picked from commit 160502f)