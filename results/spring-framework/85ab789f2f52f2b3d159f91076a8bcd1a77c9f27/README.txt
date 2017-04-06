commit 85ab789f2f52f2b3d159f91076a8bcd1a77c9f27
Author: Sam Brannen <sam@sambrannen.com>
Date:   Tue Nov 6 18:07:32 2012 +0100

    Refactor & polish DateTimeFormatterFactory[Bean]

    This commit refactors the logic in DateTimeFormatterFactory's
    createDateTimeFormatter() method to ensure forward compatibility with
    possible future changes to the ISO enum.

    This commit also polishes the Javadoc for DateTimeFormatterFactoryBean.

    Issue: SPR-9959