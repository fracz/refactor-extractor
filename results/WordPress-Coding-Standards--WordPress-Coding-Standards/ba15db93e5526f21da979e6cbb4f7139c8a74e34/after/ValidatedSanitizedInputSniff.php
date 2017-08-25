<?php
/**
 * Flag any non-validated/sanitized input ( _GET / _POST / etc. )
 *
 * PHP version 5
 *
 * @category PHP
 * @package  PHP_CodeSniffer
 * @author   Shady Sharaf <shady@x-team.com>
 * @link     https://github.com/WordPress-Coding-Standards/WordPress-Coding-Standards/issues/69
 */
class WordPress_Sniffs_VIP_ValidatedSanitizedInputSniff extends WordPress_Sniff
{

	/**
	 * Check for validation functions for a variable within its own parenthesis only
	 * @var boolean
	 */
	public $check_validation_in_scope_only = false;

	/**
	 * Returns an array of tokens this test wants to listen for.
	 *
	 * @return array
	 */
	public function register()
	{
		return array(
				T_VARIABLE,
				T_DOUBLE_QUOTED_STRING,
			   );

	}//end register()


	/**
	 * Processes this test, when one of its tokens is encountered.
	 *
	 * @param PHP_CodeSniffer_File $phpcsFile The file being scanned.
	 * @param int                  $stackPtr  The position of the current token
	 *                                        in the stack passed in $tokens.
	 *
	 * @return void
	 */
	public function process( PHP_CodeSniffer_File $phpcsFile, $stackPtr )
	{
		$this->init( $phpcsFile );
		$tokens = $phpcsFile->getTokens();
		$superglobals = WordPress_Sniff::$input_superglobals;

		// Handling string interpolation
		if ( $tokens[ $stackPtr ]['code'] === T_DOUBLE_QUOTED_STRING ) {
			foreach ( $superglobals as $superglobal ) {
				if ( false !== strpos( $tokens[ $stackPtr ]['content'], $superglobal ) ) {
					$phpcsFile->addError( 'Detected usage of a non-sanitized, non-validated input variable: %s', $stackPtr, null, array( $tokens[$stackPtr]['content'] ) );
					return;
				}
			}

			return;
		}

		// Check if this is a superglobal.
		if ( ! in_array( $tokens[$stackPtr]['content'], $superglobals ) )
			return;

		// If we're overriding a superglobal with an assignment, no need to test
		if ( $this->is_assignment( $stackPtr ) ) {
			return;
		}

		// This superglobal is being validated.
		if ( $this->is_in_isset_or_empty( $stackPtr ) ) {
			return;
		}

		// If this isn't in a function call we can perform the sanitization check easily.
		if ( ! isset( $tokens[ $stackPtr ]['nested_parenthesis'] ) ) {

			if ( $this->has_whitelist_comment( 'sanitization', $stackPtr ) ) {
				return;
			}

			// Search for casting.
			if ( ! $this->is_safe_casted( $stackPtr ) ) {
				$phpcsFile->addError( 'Detected usage of a non-sanitized input variable: %s', $stackPtr, 'InputNotSanitized', array( $tokens[$stackPtr]['content'] ) );
				return;
			}
		}

		$array_key = $this->get_array_access_key( $stackPtr );

		if ( empty( $array_key ) ) {
			return;
		}

		// Check for validation first.
		if ( ! $this->is_validated( $stackPtr, $array_key, $this->check_validation_in_scope_only ) ) {
			$phpcsFile->addError( 'Detected usage of a non-validated input variable: %s', $stackPtr, 'InputNotValidated', array( $tokens[$stackPtr]['content'] ) );
			// return; // Should we just return and not look for sanitizing functions ?
		}

		if ( $this->has_whitelist_comment( 'sanitization', $stackPtr ) ) {
			return;
		}

		// Now look for sanitizing functions
		if ( ! $this->is_sanitized( $stackPtr ) ) {
			$phpcsFile->addError( 'Detected usage of a non-sanitized input variable: %s', $stackPtr, 'InputNotSanitized', array( $tokens[$stackPtr]['content'] ) );
		}

		return;
	}//end process()

}//end class