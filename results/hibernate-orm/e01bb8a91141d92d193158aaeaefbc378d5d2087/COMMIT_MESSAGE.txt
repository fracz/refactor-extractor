commit e01bb8a91141d92d193158aaeaefbc378d5d2087
Author: Andrig Miller <andy.miller@jboss.com>
Date:   Wed May 25 17:40:49 2011 -0600

    HHH-6258:  Wrap trace and debug log statements to lessen the load on JBossLogManagerLogger.doLog.  This improved throughput quite a bit.