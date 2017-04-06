commit a95d4f4ee7e7ada4b1ddec96b8deef140f96c1a7
Author: Nik Everett <nik9000@gmail.com>
Date:   Tue Jul 19 18:31:19 2016 -0400

    Add Location header and improve REST testing

    This adds a header that looks like `Location: /test/test/1` to the
    response for the index/create/update API. The requirement for the header
    comes from https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html

    https://tools.ietf.org/html/rfc7231#section-7.1.2 claims that relative
    URIs are OK. So we use an absolute path which should resolve to the
    appropriate location.

    Closes #19079

    This makes large changes to our rest test infrastructure, allowing us
    to write junit tests that test a running cluster via the rest client.
    It does this by splitting ESRestTestCase into two classes:
    * ESRestTestCase is the superclass of all tests that use the rest client
    to interact with a running cluster.
    * ESClientYamlSuiteTestCase is the superclass of all tests that use the
    rest client to run the yaml tests. These tests are shared across all
    official clients, thus the `ClientYamlSuite` part of the name.