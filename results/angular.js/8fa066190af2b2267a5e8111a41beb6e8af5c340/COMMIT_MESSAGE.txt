commit 8fa066190af2b2267a5e8111a41beb6e8af5c340
Author: Di Peng <pengdi@google.com>
Date:   Sat Jul 9 18:15:40 2011 -0700

    refactor(gen-docs): use q, qq, q-fs (node modules) to write gen-docs

    - re-write gendocs.js, reader.js and writer.js
    - all calls are asynchronous