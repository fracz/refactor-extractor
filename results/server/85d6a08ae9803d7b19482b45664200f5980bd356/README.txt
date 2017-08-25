commit 85d6a08ae9803d7b19482b45664200f5980bd356
Author: Arthur Schiwon <blizzz@owncloud.com>
Date:   Mon Nov 5 20:47:26 2012 +0100

    prepare SQL query only once, that's what prepared statements are for. Should improve upgrade time with larger setups