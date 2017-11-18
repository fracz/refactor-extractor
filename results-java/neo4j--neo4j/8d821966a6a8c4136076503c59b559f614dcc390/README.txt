commit 8d821966a6a8c4136076503c59b559f614dcc390
Author: Martin Furmanski <martin.furmanski@neotechnology.com>
Date:   Wed Jan 11 15:54:53 2017 +0000

    improve error reporting and exception handling in read replica startup

    Apart from internal code structure, this improves troubleshooting for
    a user where a clean reason for a failed startup is visible in neo4j.log
    and thus easily actionable.