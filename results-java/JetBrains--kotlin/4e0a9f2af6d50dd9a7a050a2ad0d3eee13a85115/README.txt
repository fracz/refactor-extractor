commit 4e0a9f2af6d50dd9a7a050a2ad0d3eee13a85115
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Tue Jul 31 20:25:32 2012 +0400

    type inference errors improvement
    if constraint system without expected type was successful,
    expected type mismatch error should be generated (instead of conflicting substitutions error)
     #KT-731 fixed