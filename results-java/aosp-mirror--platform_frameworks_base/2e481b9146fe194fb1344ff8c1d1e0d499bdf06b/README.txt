commit 2e481b9146fe194fb1344ff8c1d1e0d499bdf06b
Author: Daniel Sandler <dsandler@android.com>
Date:   Tue Jan 24 14:45:40 2012 -0500

    Stop showing "No internet connection" when there is one.

    In particular, even though the mobile data network isn't
    routing packets (and therefore is not an internet
    connection), we want to show the PLMN anyway:

      [MOBILE RSSI] Carrier  [WIFI RSSI] WiFi SSID

    This change also improves the following cases:

     - Combines "No internet connection" from wifi and mobile
       into one single string in airplane mode:

         [AIRPLANE] No internet connection.

     - Removes "No internet connection" from the mobile string
       when wifi is on in airplane mode, making a nice compact
       display in this case:

         [AIRPLANE] [WIFI RSSI] WiFi SSID

    Bug: 5903914
    Change-Id: I477821d2c5e9922252dd6bcb3ed494c8c57d99b0