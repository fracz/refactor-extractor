commit bd991d03cf0135dbd6d00c5ddaf1392ff6dbc405
Author: Petr Skoda <commits@skodak.org>
Date:   Sun May 20 10:50:14 2012 +0200

    MDL-33018 add general index type hints and use PostgreSQL varchar_pattern_ops index type for context.path

    This significantly improves performance of accesslib queries,
    credit for the discovery of this solution goes to Andrew Masterton from OU.