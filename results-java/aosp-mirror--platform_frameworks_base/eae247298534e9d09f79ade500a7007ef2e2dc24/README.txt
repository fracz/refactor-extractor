commit eae247298534e9d09f79ade500a7007ef2e2dc24
Author: Tim Murray <timmurray@google.com>
Date:   Fri Dec 12 11:34:48 2014 -0800

    Enable native tracking for RS contexts to improve GC behavior.

    This should prevent apps from leaking RS contexts as easily.

    bug 18579193

    Change-Id: I2d943ce4443ce7cb90ebdd3dd37d338eda6df3a2