commit 554224f9cc39c4cbbcdaa0ff7e183a1cc416ce8e
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Dec 10 22:37:32 2007 +0000

    refactored some stuff and added tests to Stubber

    --HG--
    rename : src/org/mockito/internal/ToTypeMappings.java => src/org/mockito/internal/EmptyReturnValues.java
    rename : src/org/mockito/internal/StubbedInvocation.java => src/org/mockito/internal/StubbedInvocationMatcher.java
    rename : test/org/mockito/internal/ToTypeMappingsTest.java => test/org/mockito/internal/EmptyReturnValuesTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40156