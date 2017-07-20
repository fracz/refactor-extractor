commit a2c6d221248333d68b056902f20aeade0740b5b9
Author: Carsten Brandt <mail@cebe.cc>
Date:   Wed May 15 20:39:23 2013 +0200

    refactored web/Response::sendFile()

    - better throw http exception on not satisfiable range request
    - constitent header names
    - fixed range end when range request is to the end
    - added unit test

    related to #275, fixes #148