commit dd6874aa7adc0c735be2c33e61eb657cb730c60a
Author: Yaourt <yaourt@yaourtprod.net>
Date:   Mon Nov 8 15:57:43 2010 +0100

    Hashes hkeys and hvals refactored to return a set instead of a list.

    It looks like the order is not guaranted, so a set seems much adapted than a list.