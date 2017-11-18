commit b33227d23eb0ec3507192f94c2eee651a0f97783
Author: Yuhao Zheng <yuhaozheng@google.com>
Date:   Fri Jul 20 10:55:17 2012 -0700

    WifiWatchdog changes for poor link detection

    - use packet loss (wlutil pktcnt) instead of frame loss, retune all parameters
    - use wpa_supplicant to get packet loss counters, instead of netd
    - handle BSSID roaming in all situations
    - improve flapping avoidance mechanism by setting different target RSSI
    - handle high packet loss in high RSSI (never seen in real testing)
    - add more comments on how to set all parameters

    Signed-off-by yuhaozheng@google.com

    Change-Id: I33429f063d8625a458be4791edd83a86d5a723df