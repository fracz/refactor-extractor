commit c9220d1bb3de83c048c083681a1ea5d46c64d913
Author: Michel Fortin <michel.fortin@michelf.com>
Date:   Tue Aug 4 17:05:44 2015 -0400

    Finalization of fenced code block improvements by Mario Konrad:

    - Moving `code_block_content_func` configuration variable to regular Markdown parser, now using it to convert the content of indented code blocks to HTML too.
    - Fix for code block class name and special attribute blocks together.
    - Updated the fenced code block syntax in the HTML block parser (so it correctly skips those code blocks).
    - Updated Readme.md to explain the changes.