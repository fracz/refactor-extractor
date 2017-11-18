commit b636ca79d579dbef3965b578fa8253aa6189e263
Author: Boaz Leskes <b.leskes@gmail.com>
Date:   Sun Apr 9 22:04:12 2017 +0200

    Engine: version logic on replicas should not be hard coded (#23998)

    The refactoring in #23711 hardcoded version logic for replica to assume monotonic versions. Sadly that's wrong for `FORCE` and `VERSION_GTE`. Instead we should use the methods in VersionType to detect conflicts.

    Note - once replicas use sequence numbers for out of order delivery, this logic goes away.