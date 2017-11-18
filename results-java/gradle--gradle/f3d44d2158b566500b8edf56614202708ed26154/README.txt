commit f3d44d2158b566500b8edf56614202708ed26154
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Dec 7 21:02:52 2011 +0100

    Rationalized abstractions and implementations of Resource/ReadableResource

    -More refactorings after the pairing session.
    -Now Resource is the top element in the hierarchy and it only describes the resource.
    -Removed some internal interfaces/implementations that dealt with describing resources. Now it is not needed as every resource is described.