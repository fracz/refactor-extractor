commit 1c316fb5b7a67756b52e5389cb65ce84c3273cdf
Author: Ralph Schindler <ralph.schindler@zend.com>
Date:   Mon Aug 1 08:53:13 2011 -0500

    Zend\Http Request and Response refactoring:
      - added tests for various objects
      - moved request line and response line into request and response object out of header object
    Zend\Stdlib:
      - refactored Message and Parameters for generalized usage
      - added tests
    Zend\Uri\UriFactory
      - fixed register scheme method to use proper property