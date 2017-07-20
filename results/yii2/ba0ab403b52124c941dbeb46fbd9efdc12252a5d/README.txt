commit ba0ab403b52124c941dbeb46fbd9efdc12252a5d
Author: Robert Korulczyk <robert@korulczyk.pl>
Date:   Mon Jun 12 11:25:45 2017 +0200

    Added php-cs-fixer coding standards validation to Travis CI (#14100)

    * php-cs-fixer: PSR2 rule.

    * php-cs-fixer: PSR2 rule - fix views.

    * Travis setup refactoring.

    * Add php-cs-fixer to travis cs tests.

    * Fix tests on hhvm-3.12

    * improve travis config

    * composer update

    * revert composer update

    * improve travis config

    * Fix CS.

    * Extract config to separate classes.

    * Extract config to separate classes.

    * Add file header.

    * Force short array syntax.

    * binary_operator_spaces fixer

    * Fix broken tests

    * cast_spaces fixer

    * concat_space fixer

    * dir_constant fixer

    * ereg_to_preg fixer

    * function_typehint_space fixer

    * hash_to_slash_comment fixer

    * is_null fixer

    * linebreak_after_opening_tag fixer

    * lowercase_cast fixer

    * magic_constant_casing fixer

    * modernize_types_casting fixer

    * native_function_casing fixer

    * new_with_braces fixer

    * no_alias_functions fixer

    * no_blank_lines_after_class_opening fixer

    * no_blank_lines_after_phpdoc fixer

    * no_empty_comment fixer

    * no_empty_phpdoc fixer

    * no_empty_statement fixer

    * no_extra_consecutive_blank_lines fixer

    * no_leading_import_slash fixer

    * no_leading_namespace_whitespace fixer

    * no_mixed_echo_print fixer

    * no_multiline_whitespace_around_double_arrow fixer

    * no_multiline_whitespace_before_semicolons fixer

    * no_php4_constructor fixer

    * no_short_bool_cast fixer

    * no_singleline_whitespace_before_semicolons fixer

    * no_spaces_around_offset fixer

    * no_trailing_comma_in_list_call fixer

    * no_trailing_comma_in_singleline_array fixer

    * no_unneeded_control_parentheses fixer

    * no_unused_imports fixer

    * no_useless_return fixer

    * no_whitespace_before_comma_in_array fixer

    * no_whitespace_in_blank_line fixer

    * not_operator_with_successor_space fixer

    * object_operator_without_whitespace fixer

    * ordered_imports fixer

    * php_unit_construct fixer

    * php_unit_dedicate_assert fixer

    * php_unit_fqcn_annotation fixer

    * phpdoc_indent fixer

    * phpdoc_no_access fixer

    * phpdoc_no_empty_return fixer

    * phpdoc_no_package fixer

    * phpdoc_no_useless_inheritdoc fixer

    * Fix broken tests

    * phpdoc_return_self_reference fixer

    * phpdoc_single_line_var_spacing fixer

    * phpdoc_single_line_var_spacing fixer

    * phpdoc_to_comment fixer

    * phpdoc_trim fixer

    * phpdoc_var_without_name fixer

    * psr4 fixer

    * self_accessor fixer

    * short_scalar_cast fixer

    * single_blank_line_before_namespace fixer

    * single_quote fixer

    * standardize_not_equals fixer

    * ternary_operator_spaces fixer

    * trailing_comma_in_multiline_array fixer

    * trim_array_spaces fixer

    * protected_to_private fixer

    * unary_operator_spaces fixer

    * whitespace_after_comma_in_array fixer

    * `parent::setRules()` -> `$this->setRules()`

    * blank_line_after_opening_tag fixer

    * Update finder config.

    * Revert changes for YiiRequirementChecker.

    * Fix array formatting.

    * Add missing import.

    * Fix CS for new code merged from master.

    * Fix some indentation issues.