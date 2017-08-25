commit 914788c2221e3bee78dbf6f454b88b507ca491d8
Author: Edan Schwartz <edan@edanschwartz.com>
Date:   Tue Mar 3 07:51:33 2015 -0600

    Minor refactor of FuncTestCase::tearDownTestProject

    - use existing FS service
    - check project dir exists before removal