commit f92a27a92409ca4367333287c36b474b1826ea44
Author: Kay Roepke <kay.roepke@xing.com>
Date:   Sun May 6 01:11:35 2012 +0400

    refactored GELFMessage to use enum for types and magic bytes instead of loose constants

    made determining the type ever so slightly more efficient.

    turned payload array (and other fields and parameters) into final, this should lead to more efficient inlining