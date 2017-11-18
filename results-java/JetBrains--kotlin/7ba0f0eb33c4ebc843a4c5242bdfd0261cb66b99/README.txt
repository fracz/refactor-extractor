commit 7ba0f0eb33c4ebc843a4c5242bdfd0261cb66b99
Author: develar <develop@gmail.com>
Date:   Thu Aug 8 19:51:49 2013 +0400

    JS backend: refactoring
    - add KotlinFunctionIntrinsic;
    - remove CallStandardMethodIntrinsic
    - rename TranslationUtils#generateCallArgumentList to TranslationUtils#generateInvocationArguments;
    - add to PatternBuilder#pattern with root(prefix) parameter.