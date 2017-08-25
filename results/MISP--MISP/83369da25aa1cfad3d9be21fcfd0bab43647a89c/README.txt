commit 83369da25aa1cfad3d9be21fcfd0bab43647a89c
Author: iglocska <andras.iklody@gmail.com>
Date:   Thu Aug 28 14:27:45 2014 +0200

    Several fixes including compatibility with the STIX to_xml() performance fix

    - STIX export performance greatly improved thanks to 84ce8d8be6376797053668d68e1b863713f008dd
    - some junk removed
    - fixed some minor pagination issues on the event view
    - site admin dummy event creator now has target-* type attributes