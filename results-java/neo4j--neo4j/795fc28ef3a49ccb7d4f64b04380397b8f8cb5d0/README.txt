commit 795fc28ef3a49ccb7d4f64b04380397b8f8cb5d0
Author: Mikhaylo Demianenko <mikhaylo.demianenko@neotechnology.com>
Date:   Mon Nov 2 16:43:06 2015 +0100

    Neo4j upgrade to lucene 5.

    To be able to use new lucene features and improvements,
    cleanup legacy code that was written for old no longer supported 3.6.2 version
    we giving a try to migrate neo to latest 5.x available version.

    This changeset represent only part of required work and includes:
    changes done by Paul Horn, fix of failed tests (excluding upgrade testing - those are disabled)

    This change does not include cleanups/rethinking/logic update/compatibility code and should only be treated as baseline.