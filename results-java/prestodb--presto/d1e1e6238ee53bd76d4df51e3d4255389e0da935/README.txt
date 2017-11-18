commit d1e1e6238ee53bd76d4df51e3d4255389e0da935
Author: Andrii Rosa <Andriy.Rosa@TERADATA.COM>
Date:   Tue Feb 9 13:01:55 2016 +0100

    Migrate TestSignature and TestTypeSignature

    As the signature parameters binging code has been moved to SignatureBinder,
    all the parameters binding tests moved to the TestSignatureBinder class.

    Assertions in TestSignatureBinder has been refactored in "assertj style".

    This resolves #4405