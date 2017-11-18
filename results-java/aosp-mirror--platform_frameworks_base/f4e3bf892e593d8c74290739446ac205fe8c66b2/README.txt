commit f4e3bf892e593d8c74290739446ac205fe8c66b2
Author: Chia-chi Yeh <chiachi@android.com>
Date:   Thu Jun 30 12:33:17 2011 -0700

    VPN: refactor few JNI methods for the usage of legacy VPN.

    Now default routes are handled in JNI instead of VpnBuilder.

    Change-Id: Ib026bba6793b64aae0f8356df3d2aaae489d08b4