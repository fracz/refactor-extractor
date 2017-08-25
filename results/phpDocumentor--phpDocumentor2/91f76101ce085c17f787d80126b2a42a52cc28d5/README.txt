commit 91f76101ce085c17f787d80126b2a42a52cc28d5
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Sun Nov 4 15:28:05 2012 +0100

    Add getter/setter for Exporters in Parser

    In this commit we have added a getter and setter for the dynamic determination
    of exporters. Prior to this commit a specific exporter was hard-coded.

    During this commit several refactoring actions were executed, such as splitting
    methods into sub-methods and adding more unit tests for the parser.