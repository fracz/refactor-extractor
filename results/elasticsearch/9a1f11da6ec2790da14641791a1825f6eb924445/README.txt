commit 9a1f11da6ec2790da14641791a1825f6eb924445
Author: Karel Minarik <karmi@elasticsearch.com>
Date:   Sat May 2 12:54:22 2015 +0200

    Trimmed the main `elasticsearch.yml` configuration file

    The main `elasticsearch.yml` file mixed configuration, documentation
    and advice together.

    Due to a much improved documentation at <http://www.elastic.co/guide/>,
    the content has been trimmed, and only the essential settings have
    been left, to prevent the urge to excessive over-configuration.

    Related: 8d0f1a7d123f579fc772e82ef6b9aae08f6d13fd