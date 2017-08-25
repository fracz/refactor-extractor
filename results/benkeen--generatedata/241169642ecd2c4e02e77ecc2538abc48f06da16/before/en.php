<?php

$L = array();

$L["Tree_auto_increment_row_num"] = "Auto-increment row number:";
$L["Tree_help_1"] = "This data type lets you generate tree-like data in which every row is a child of another row - except the very first row, which is the trunk of the tree. This data type must be used in conjunction with the Auto-Increment data type: that ensures that every row has a unique numeric value, which this data type uses to reference the parent rows.";
$L["Tree_help_2"] = "The options let you specify which of your form fields is the appropriate auto-increment field and the maximum number of children a node may have.";
$L["Tree_invalid_parent"] = "[invalid parent]";
$L["Tree_max_num_sibling_nodes"] = "Max number of sibling nodes:";
$L["Tree_name"] = "Tree (parent row ID)";