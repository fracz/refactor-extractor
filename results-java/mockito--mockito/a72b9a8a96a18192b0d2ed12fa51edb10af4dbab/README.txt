commit a72b9a8a96a18192b0d2ed12fa51edb10af4dbab
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Nov 9 22:03:24 2009 +0000

    Fixed issue 142
    In order to provide best feedback when dev misuses jUnit I forward initializer jUnit exceptions (for example: @BeforeClass must be static). This way exceptions from jUnit are correctly handled by IDE (previously, confusing Mockito exception was thrown).
    Some tiny refactorings according to IDEA warnings.

    --HG--
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%401673