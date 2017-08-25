commit 3329f44a76f960f7b73beb6bb4d717b10bfca28b
Author: Morris Jobke <hey@morrisjobke.de>
Date:   Tue Mar 21 16:08:05 2017 -0600

    Address comments

    * fix URL to documentation
    * improve logic of UTF8mb4 check
    * fix connection parameter creation - it's done already in ConnectionFactory::createConnectionParams
    * remove unused attributes of MDB2SchemaReader

    Signed-off-by: Morris Jobke <hey@morrisjobke.de>