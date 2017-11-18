commit 0f1a908bae7f4703df302a6ee8e1f9348f7584e4
Author: Shay Banon <kimchy@gmail.com>
Date:   Fri Dec 30 02:08:05 2011 +0200

    improve multi field highlighting with fast vector based highlighting by caching the custom query across field mappers (one when there is field match, and one when there isn't)