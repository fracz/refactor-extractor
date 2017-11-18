commit b993e3ab09718ea7b74f109821ff84cff4bb6a26
Author: Mattias Persson <mattias@neotechnology.com>
Date:   Tue Jan 24 16:00:51 2012 +0100

    Caches the tx start position prior to committing it so that ZK last committed update setter has fast access to it. Should be refactored later