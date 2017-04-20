commit 2e095b4140724b11118f2ed458a34e3d8ceddfad
Author: Brian Vaughn <brian.david.vaughn@gmail.com>
Date:   Thu Feb 9 14:10:45 2017 -1000

    Hardened validateCallback to better handle null values (#8973)

    Previously, calls to validateCallback() with a null callback value resulted in runtime errors if a certain transform was not applied prior to running. This commit wraps the invariant() with the condition check so as to avoid calling formatUnexpectedArgument() unless necessary. It also replaces the truthy/falsy callback check with an explicit check for improved performance.