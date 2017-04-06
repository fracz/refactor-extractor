commit 962ce9a7e6b24fd667b848571253854e9d9fb9b6
Author: kimchy <kimchy@gmail.com>
Date:   Sat Apr 3 03:03:37 2010 +0300

    improve initial read from gateway on first master startup, first master will not complete the startup sequence until meta data and all indices have been created from the gateway