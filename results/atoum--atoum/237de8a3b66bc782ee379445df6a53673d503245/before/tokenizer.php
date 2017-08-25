<?php

namespace mageekguy\atoum\php;

use
	\mageekguy\atoum\exceptions,
	\mageekguy\atoum\php\tokenizer,
	\mageekguy\atoum\php\tokenizer\iterators
;

class tokenizer implements \iteratorAggregate
{
	protected $iterator = null;

	public function __construct()
	{
		$this->resetIterator();
	}

	public function getIterator()
	{
		return $this->iterator;
	}

	public function resetIterator()
	{
		$this->iterator = new iterators\phpScript();

		return $this;
	}

	public function tokenize($string)
	{
		$currentClass = null;
		$currentProperty = null;
		$currentFunction = null;
		$currentMethod = null;
		$currentAbstractMethod = null;
		$currentArgument = null;
		$currentConstant = null;
		$currentNamespace = null;
		$currentNamespaceImportation = null;
		$currentDefaultValue = null;
		$currentIterator = $this->iterator;

		foreach ($tokens = new \arrayIterator(token_get_all($string)) as $key => $token)
		{
			switch ($token[0])
			{
				case T_USE:
					$currentIterator->appendNamespaceImportation($currentNamespaceImportation = new iterators\phpNamespace\importation());
					$currentIterator = $currentNamespaceImportation;
					break;

				case T_NAMESPACE:
					if ($currentNamespace !== null)
					{
						$currentIterator = $currentNamespace->getParent();
						$currentNamespace = null;
					}

					$currentIterator->appendNamespace($currentNamespace = new iterators\phpNamespace());
					$currentIterator = $currentNamespace;
					break;

				case T_ABSTRACT:
					if ($currentClass === null)
					{
						$currentIterator->appendClass($currentClass = new iterators\phpClass());
						$currentIterator = $currentClass;
					}
					else
					{
						$currentIterator->appendMethod($currentAbstractMethod = new iterators\phpMethod());
						$currentIterator = $currentAbstractMethod;
					}
					break;

				case T_FINAL:
					if ($currentClass === null)
					{
						$currentIterator->appendClass($currentClass = new iterators\phpClass());
					}
						$currentIterator = $currentClass;
					break;

				case T_CLASS:
					if ($currentClass === null)
					{
						$currentIterator->appendClass($currentClass = new iterators\phpClass());
						$currentIterator = $currentClass;
					}
					break;

				case T_VARIABLE:
					if ($currentMethod !== null && $currentArgument === null)
					{
						$currentIterator->appendArgument($currentArgument = new iterators\phpArgument());
						$currentIterator = $currentArgument;
					}
					else if ($currentClass !== null && $currentProperty === null)
					{
						$currentIterator->appendProperty($currentProperty = new iterators\phpProperty());
						$currentIterator = $currentProperty;
					}
					break;

				case T_CONST:
					$currentIterator->appendConstant($currentConstant = new iterators\phpConstant());
					$currentIterator = $currentConstant;
					break;

				case T_FUNCTION:
					if ($currentClass !== null && $currentMethod === null && $currentAbstractMethod === null)
					{
						$currentIterator->appendMethod($currentMethod = new iterators\phpMethod());
						$currentIterator = $currentMethod;
					}
					else if ($currentMethod === null && $currentAbstractMethod === null)
					{
						$currentIterator->appendFunction($currentFunction = new iterators\phpFunction());
						$currentIterator = $currentFunction;
					}
					break;

				case T_PUBLIC:
				case T_PRIVATE:
				case T_PROTECTED:
					if ($currentClass !== null)
					{
						if (self::nextTokenIs(T_FUNCTION, $tokens) === true)
						{
							$currentIterator->appendMethod($currentMethod = new iterators\phpMethod());
							$currentIterator = $currentMethod;
						}
						else
						{
							$currentIterator->appendProperty($currentProperty = new iterators\phpProperty());
							$currentIterator = $currentProperty;
						}
					}
					break;

				case ';':
					if ($currentNamespaceImportation !== null)
					{
						$currentIterator = $currentNamespaceImportation->getParent();
						$currentConstant = null;
					}
					else if ($currentAbstractMethod !== null)
					{
						$currentIterator = $currentAbstractMethod->getParent();
						$currentAbstractMethod = null;
					}
					else if ($currentConstant !== null)
					{
						$currentIterator = $currentConstant->getParent();
						$currentConstant = null;
					}
					else if ($currentProperty !== null)
					{
						$currentIterator = $currentProperty->getParent();
						$currentProperty = null;
					}
					break;

				case ',':
					if ($currentDefaultValue !== null)
					{
						$currentIterator = $currentDefaultValue->getParent();
						$currentDefaultValue = null;
					}
					else if ($currentArgument !== null)
					{
						$currentIterator = $currentArgument->getParent();
						$currentArgument = null;
					}
					else if ($currentProperty !== null)
					{
						$currentIterator = $currentProperty->getParent();
						$currentProperty = null;
					}
					break;

				case ')':
					if ($currentArgument !== null)
					{
						$currentIterator = $currentArgument->getParent();
						$currentArgument = null;
					}
					break;

				case T_CLOSE_TAG:
					if ($currentNamespace !== null)
					{
						if (self::nextTokenIs(T_OPEN_TAG, $tokens) === false)
						{
							$currentIterator = $currentNamespace->getParent();
							$currentNamespace = null;
						}
					}
					break;
			}

			$currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));

			switch ($token[0])
			{
				case '}':
					if ($currentMethod !== null)
					{
						$currentIterator = $currentMethod->getParent();
						$currentMethod = null;
					}
					else if ($currentClass !== null)
					{
						$currentIterator = $currentClass->getParent();
						$currentClass = null;
					}
					else if ($currentFunction !== null)
					{
						$currentIterator = $currentFunction->getParent();
						$currentFunction = null;
					}
					break;

				case '=':
					if ($currentArgument !== null)
					{
						self::appendCommentAndWhitespace($currentIterator, $tokens);

						$currentIterator->appendDefaultValue($currentDefaultValue = new iterators\phpDefaultValue());
						$currentIterator = $currentDefaultValue;
					}
					break;

				case T_ARRAY:
					self::appendArray($currentIterator, $tokens);
					break;
			}
		}

		return $this;
	}

	protected static function appendCommentAndWhitespace(tokenizer\iterator $iterator, \arrayIterator $tokens)
	{
		$key = $tokens->key();

		while (isset($tokens[$key + 1]) === true && ($tokens[$key + 1][0] === T_WHITESPACE || $tokens[$key + 1][0] === T_COMMENT))
		{
			$tokens->next();

			$token = $tokens->current();

			$iterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));

			$key = $tokens->key();
		}
	}

	protected static function nextTokenIs($tokenName, \arrayIterator $tokens, array $skipedTags = array(T_WHITESPACE, T_COMMENT, T_INLINE_HTML))
	{
		$key = $tokens->key() + 1;

		while (isset($tokens[$key]) === true && in_array($tokens[$key], $skipedTags) === true)
		{
			$key++;
		}

		$key++;

		return (isset($tokens[$key]) === true && $tokens[$key][0] === $tokenName);
	}

	protected static function appendArray(tokenizer\iterator $iterator, \arrayIterator $tokens)
	{
		self::appendCommentAndWhitespace($iterator, $tokens);

		$tokens->next();

		if ($tokens->valid() === true)
		{
			$token = $tokens->current();

			if ($token[0] === '(')
			{
				$iterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));

				$stack = 1;

				while ($stack > 0 && $tokens->valid() === true)
				{
					$tokens->next();

					if ($tokens->valid() === true)
					{
						$token = $tokens->current();

						if ($token[0] === '(')
						{
							$stack++;
						}
						else if ($token[0] === ')')
						{
							$stack--;
						}

						$iterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));
					}
				}
			}
		}
	}
}

?>