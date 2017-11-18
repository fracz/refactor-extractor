commit b7e26fb1ad1802025cc3a01eeba6acfbc8f3f444
Author: Jeff Davidson <jpd@google.com>
Date:   Thu Apr 7 12:47:20 2016 -0700

    Move CarrierAppUtils into frameworks/base/telephony.

    This is a no-op refactoring which will allow us to access
    CarrierAppUtils from PackageManagerService.

    Bug: 27821069
    Change-Id: Id6ac33020395f7fc03b285ffa8c3d421a02270ec