commit 73c58693cd5cac0f81778eb48143ca3f3fbb915b
Author: Shay Banon <kimchy@gmail.com>
Date:   Thu Dec 29 13:57:39 2011 +0200

    improve analyzers and tokenizers bindings to work similar to filters and char filters, by processing them first, and lazily binding them if needed