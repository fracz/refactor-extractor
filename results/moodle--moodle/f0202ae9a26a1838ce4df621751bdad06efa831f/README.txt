commit f0202ae9a26a1838ce4df621751bdad06efa831f
Author: Petr Skoda <commits@skodak.org>
Date:   Sun Apr 22 17:17:27 2012 +0200

    MDL-30686 improve accuracy of qualified_me() by trying PAGE->url first

    This should help SSL proxies and returning to current page after require_login().