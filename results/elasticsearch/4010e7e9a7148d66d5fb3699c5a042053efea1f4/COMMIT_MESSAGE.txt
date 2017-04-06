commit 4010e7e9a7148d66d5fb3699c5a042053efea1f4
Author: javanna <cavannaluca@gmail.com>
Date:   Fri Aug 14 10:47:38 2015 +0200

    Java api: restore support for minimumShouldMatch and disableCoord in TermsQueryBuilder

     TermsQueryParser still parses those values although deprecated. These need to be present in the java api as well to get ready for the query refactoring, where the builders are the intermediate query format that we parse our json queries into. Whatever the parser supports need to be supported by the builder as well.

    Closes #12870