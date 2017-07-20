commit d854f07ee2678d79d5fadf599b951f66d9724a8d
Author: Alan <me@alanhollis.com>
Date:   Thu May 2 20:28:19 2013 +0200

    Enable PDO persistent connections

    Turn on persistent connections. Seems to result in noticeable speed improvements in my very unscientific local apachebench results.