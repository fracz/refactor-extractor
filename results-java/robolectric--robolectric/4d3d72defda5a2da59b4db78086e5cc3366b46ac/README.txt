commit 4d3d72defda5a2da59b4db78086e5cc3366b46ac
Author: Jonathan Gerrish <jonathan@indiekid.org>
Date:   Tue Jun 21 10:26:23 2016 -0700

    refactor attrToTypeArray (#2525)

    Cleanup ShadowResources.attrsToTypedArray() - push down to ShadowAssetManager and switch buildAttributes to return a singlular Attribute for a given ID, rather than a list of Attributes for the whole int[] array. This saves one extra iteration of the int[] array and one search across the intermediate List<Attributes>.

    Delete a bunch of (now) unused Attribute.find() / remove() / put() methods for List<Attribute> operations.