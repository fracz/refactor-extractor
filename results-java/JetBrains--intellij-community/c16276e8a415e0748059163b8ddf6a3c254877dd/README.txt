commit c16276e8a415e0748059163b8ddf6a3c254877dd
Author: Denis Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Thu Apr 28 13:00:48 2011 +0400

    IDEA-68536 I want Live Templates to automatically add static imports (Use Case: templates for org.junit.Assert.assertXXX)

    1. Corresponding template optional processor is added;
    2. 'Add static import' actions are refactored in order to provide ability to execute their logic from the template processor;