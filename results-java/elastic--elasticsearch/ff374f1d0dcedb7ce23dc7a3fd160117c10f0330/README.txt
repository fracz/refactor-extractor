commit ff374f1d0dcedb7ce23dc7a3fd160117c10f0330
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Thu May 18 17:53:28 2017 +0200

    TransportShardBulkAction: remove hard version assertion and improve todo comment

    We have decided not to force a future version upgrade to deal with this todo. Rather, we'll keep the code until its in our way / the opportunity arises to deal with it.