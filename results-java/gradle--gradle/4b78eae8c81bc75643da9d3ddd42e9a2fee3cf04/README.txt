commit 4b78eae8c81bc75643da9d3ddd42e9a2fee3cf04
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Dec 16 15:48:38 2011 +0100

    Tooling api improvements around compatibility...

    -Started decorating BuildOperationParametersVersion1 instances in a standard way we do it in the Tooling API. This way throw meaningful exceptions when unsupported methods are accessed. In future it will help us because decorating proxy (ProtocolToModelAdapter) will get smarter and will have more 'compatibility' oriented utility methods.
    -Changed the CompatibilityChecker so that it works in a similar fashion as the ProtocolToModelAdapter that throws IncompatibleVersionException when the some method is not supported on given Gradle version.
    -For backwards compatibility reasons I still have to maintain CompatibilityChecker because older versions of tooling api did not use ProtocolToModelAdapter to create instances of BuildOperationParametersVersion1.