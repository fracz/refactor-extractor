commit 71417552c7651a5b41e73460d0577615864aafe8
Author: Jason Gustafson <jason@confluent.io>
Date:   Thu Aug 24 16:03:55 2017 -0700

    KAFKA-5342; Clarify producer fatal/abortable errors and fix inconsistencies

    This patch improves documentation on the handling of errors for the idempotent/transactional producer. It also fixes a couple minor inconsistencies and improves test coverage. In particular:
    - UnsupportedForMessageFormat should be a fatal error for TxnOffsetCommit responses
    - UnsupportedVersion should be fatal for Produce responses and should be returned instead of InvalidRequest

    Author: Jason Gustafson <jason@confluent.io>

    Reviewers: Apurva Mehta <apurva@confluent.io>, Ismael Juma <ismael@juma.me.uk>

    Closes #3716 from hachikuji/KAFKA-5342