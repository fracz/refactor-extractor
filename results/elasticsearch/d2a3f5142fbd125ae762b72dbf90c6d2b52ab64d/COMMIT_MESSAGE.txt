commit d2a3f5142fbd125ae762b72dbf90c6d2b52ab64d
Author: kimchy <kimchy@gmail.com>
Date:   Tue Feb 1 23:10:15 2011 +0200

    improve the order of shutdown of top level components in node, close indices first and applying cluster changes, also, improve atomicity of closing of indices and shards