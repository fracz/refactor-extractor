commit fbb88e8c76e304fb372aca6060d73cf4f944bbed
Author: Thomas Darimont <tdarimont@gopivotal.com>
Date:   Fri Jul 26 11:31:50 2013 +0200

    DATAJPA-346 - EclipseLink specific workaround for joins in QueryUtils.

    Added fix in QueryUtils to work around an EclipseLinks specialty to add strict joins on a call to root.get(…) even if a later root.join(…, JoinType.LEFT) should trump this.

    We needed the first call to examine the metamodel of the path obtained to decide whether to join at all in next steps. We now work around this issue by doing a lot of ugly type checking and casting on the Metamodel directly.

    We filed https://bugs.eclipse.org/bugs/show_bug.cgi?id=413892 to maybe let EclipseLink improve at that point.

    Original pull request: #30.