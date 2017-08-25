commit 6dae36dfb3dc3007dc618353667bbb78e4be6fd1
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Thu Mar 9 06:33:45 2017 +0100

    Make the impact of the errorcode change slightly less.

    A number of sniffs use capitalization in the errorcodes to improve readability. This was unnecessarily undone by the `string_to_errorcode()` implementation.