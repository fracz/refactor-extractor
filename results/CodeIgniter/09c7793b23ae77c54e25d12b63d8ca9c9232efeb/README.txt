commit 09c7793b23ae77c54e25d12b63d8ca9c9232efeb
Author: Derek Jones <derek.jones@ellislab.com>
Date:   Tue Aug 31 13:17:10 2010 -0500

    Significant changes to the Encryption library

    - Removed double-encoding with XOR scheme when Mcrypt is available.  Additional obfuscation was not significantly aiding security, and came at a very high performance cost.
    - Changed the default encryption mode from ECB to CBC for much improved security
    - Added an encode_from_legacy() method to allow re-encoding of permanent data that was originally encoded with the older methods.