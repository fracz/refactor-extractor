commit 9e0d81e8439089845b7ddee21676f3f427a34cca
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Tue Feb 10 10:45:55 2015 -0800

    Added unit test for RegisteredServicesCache

    Minor refactoring of RegisteredServicesCache for testability. Added
    RegisteredServicesCacheTest which uses a mock version of
    RegisteredServicesCache.

    Bug:19321135
    Change-Id: If18b794b28f03b4bf4bbdfbba9e9a57e808aaebf