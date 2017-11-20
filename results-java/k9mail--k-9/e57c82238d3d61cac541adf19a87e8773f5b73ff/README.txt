commit e57c82238d3d61cac541adf19a87e8773f5b73ff
Author: Vincent Breitmoser <valodim@mugenguild.com>
Date:   Mon May 2 16:19:37 2016 +0200

    some refactorings for Message*Extractor classes

    This commit contains no semantic changes. One significant change is the
    return type of MessageExtractor.findViewablesAndAttachments, which was
    changed from a mixed return type (viewables as return value, attachments
    as output parameter) to two output parameters, both for consistency and
    performance.