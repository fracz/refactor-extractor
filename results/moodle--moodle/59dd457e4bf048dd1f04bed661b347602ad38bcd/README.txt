commit 59dd457e4bf048dd1f04bed661b347602ad38bcd
Author: Penny Leach <penny@liip.ch>
Date:   Thu Dec 3 14:26:37 2009 +0000

    portfolio: MDL-21030 - leap2a portfolio format support.

    This commit includes:

    - leap2a portfolio format, and xml writer
    - proof of concept implementation in forum and assignment modules
    - a lot of refactoring of the portfolio formats in general:
        - addition of "abstract" formats - this is necessary for plugins to be able to support groups of formats
        - addition of the idea of portfolio formats conflicting with eachother - eg richhtml & plainhtml

    it touches modules other than assignment and forum, because the format api
    changed and now each place in moodle that exports portfolio content has to deal
    with the formats it supports slightly differently.

    At the moment the Mahara portfolio still doesn't support this format, because I
    haven't done the Mahara side yet. The "file download" plugin supports it
    though.

    Still todo:

    - Add support for the other places in Moodle (glossary, data, etc)
    - Write tests, once the rest of the portfolio tests have been updated to use the new DB mocking stuff
    - Fix a bunch of TODOs