commit 40dffd01993a97e092c948d3a4769938cd8ae8eb
Author: Roberto Franchini <ro.franchini@gmail.com>
Date:   Thu Sep 29 11:16:31 2016 +0200

    refactor ETL moving databases references to a dedicated class to enable unti testing of components

    - new OETLDatabaseProvider
    - review of interfaces
    - first unit test  without json configuration on OFieldTransformerTest
    - various java8 idioms use