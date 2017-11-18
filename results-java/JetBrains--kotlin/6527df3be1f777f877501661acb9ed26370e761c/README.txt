commit 6527df3be1f777f877501661acb9ed26370e761c
Author: Svetlana Isakova <svetlana.isakova@jetbrains.com>
Date:   Mon Aug 20 20:05:45 2012 +0400

    generateBlock() refactored

    - list of statements is traversed only once
    - code duplication removed
    - variable scopes start at declarations