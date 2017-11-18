commit 572173a8f822cf1f60aecbcb8160ac981a8e9bba
Author: Nikolay Krasko <nikolay.krasko@jetbrains.com>
Date:   Fri Oct 5 14:46:59 2012 +0400

    Fields alternative signature processing with refactoring of AlternativeSignatureData

    Refactoring details:
    - Move and rename AlternativeSignatureData to kotlinSignature.AlternativeMethodSignatureData
    - Extract TypeTransforming visitor
    - Extract AlternativeSignatureMistmatchException
    - Move errors, return type, and syntax processing to base class