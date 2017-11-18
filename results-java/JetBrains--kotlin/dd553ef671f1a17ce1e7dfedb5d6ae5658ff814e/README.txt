commit dd553ef671f1a17ce1e7dfedb5d6ae5658ff814e
Author: Zalim Bashorov <Zalim.Bashorov@jetbrains.com>
Date:   Thu Oct 3 15:50:38 2013 +0400

    JPS plugin: refactoring:
    - extract utility methods from KotlinBuilder;
    - use StringUtil#join instead for iteration;
    - add private constructor to LibraryUtils.