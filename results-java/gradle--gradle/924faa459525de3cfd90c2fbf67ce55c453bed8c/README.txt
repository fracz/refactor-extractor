commit 924faa459525de3cfd90c2fbf67ce55c453bed8c
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Sat May 26 01:56:39 2012 +0200

    fixed support for groovyc optimization options

    - canonicalize boolean values to Boolean.TRUE/Boolean.FALSE
    - added and improved tests
    - test against Groovy 2.0.0-beta-3-indy