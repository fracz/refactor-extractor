commit 312535bcd50d492f7081dc401ffdff9080fcbb6e
Author: Phillip Webb <pwebb@gopivotal.com>
Date:   Thu Jan 16 10:38:56 2014 -0800

    Add SpringNamingStrategy to improve FK names

    Add a new `SpringNamingStrategy` hibernate `NamingStrategy` that
    extends `ImprovedNamingStrategy` to improve the name of foreign
    key columns.

    Fixes gh-213