commit 4dffef09431a12bb0b793ae422ec44b79d631771
Author: Oliver Gierke <ogierke@gopivotal.com>
Date:   Thu Jun 20 12:06:42 2013 +0200

    DATAJPA-348 - Extending workaround for HHH-6951.

    We're now using AnnotationUtils.findAnnotation(…) in case of Hibernate to improve workaround for HHH-6951. Added id type caching as the annotation lookup is potentially expensive.

    Added integration tests for Hibernate and EclipseLink.