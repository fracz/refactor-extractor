commit 37433638271718344498d695d5da08db12c24eed
Author: Jason Gustafson <jason@confluent.io>
Date:   Fri May 26 09:41:17 2017 +0100

    MINOR: Preserve the base offset of the original record batch in V2

    The previous code did not handle this correctly if a batch was
    compacted more than once.

    Also add test case for duplicate check after log cleaning and
    improve various comments.

    Author: Jason Gustafson <jason@confluent.io>

    Reviewers: Ismael Juma <ismael@juma.me.uk>

    Closes #3145 from hachikuji/minor-improve-base-sequence-docs