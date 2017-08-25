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

	private $tokens = null;
	private $currentIterator = null;

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
		$this->currentIterator = $this->iterator;

		foreach ($this->tokens = new \arrayIterator(token_get_all($string)) as $key => $token)
		{
			switch ($token[0])
			{
				case T_USE:
					$token = $this->appendNamespaceImportation();
					break;

				case T_NAMESPACE:
					$token = $this->appendNamespace();
					break;
			}

			$this->currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));
		}

		return $this;
	}

	private function appendNamespaceImportation()
	{
		$this->currentIterator->appendNamespaceImportation($this->currentNamespaceImportation = new iterators\phpNamespace\importation());
		$this->currentIterator = $this->currentNamespaceImportation;

		$inNamespaceImportation = true;

		while ($inNamespaceImportation === true && $this->tokens->valid() === true)
		{
			$token = $this->tokens->current();

			switch ($token[0])
			{
				case ';':
				case ',':
					$this->currentIterator = $this->currentNamespaceImportation->getParent();
					$this->currentNamespaceImportation = null;
					$inNamespaceImportation = false;
					break;

				default:
					$this->currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));
					$this->tokens->next();
			}
		}

		return $this->tokens->valid() === false ? null : $this->tokens->current();
	}

	private function appendNamespace()
	{
		$parent = $this->currentIterator->getParent();

		if ($parent !== null)
		{
			$this->currentIterator = $parent;
		}

		$this->currentIterator->appendNamespace($this->currentNamespace = new iterators\phpNamespace());
		$this->currentIterator = $this->currentNamespace;

		$inNamespace = true;

		while ($inNamespace === true && $this->tokens->valid() === true)
		{
			$token = $this->tokens->current();

			switch ($token[0])
			{
				case T_CONST:
					$this->appendConstant();
					break;

				case T_FUNCTION:
					$this->appendFunction();
					break;

				case T_FINAL:
				case T_ABSTRACT:
				case T_CLASS:
					$this->appendClass();
					break;

				case T_INTERFACE:
					$this->appendInterface();
					break;

				case T_CLOSE_TAG:
					if ($this->nextTokenIs(T_OPEN_TAG) === false)
					{
						$this->currentIterator = $this->currentIterator->getParent();
						$inNamespace = false;
					}
					break;

				case '}':
					$this->currentIterator = $this->currentIterator->getParent();
					$inNamespace = false;
					break;
			}

			$this->currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));

			$this->tokens->next();
		}

		return $this->tokens->valid() === false ? null : $this->tokens->current();
	}

	private function appendCommentAndWhitespace()
	{
		$key = $this->tokens->key();

		while (isset($this->tokens[$key + 1]) === true && ($this->tokens[$key + 1][0] === T_WHITESPACE || $this->tokens[$key + 1][0] === T_COMMENT))
		{
			$this->tokens->next();

			$token = $this->tokens->current();

			$this->currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));

			$key = $this->tokens->key();
		}
	}


	private function appendArray()
	{
		$this->appendCommentAndWhitespace();

		$this->tokens->next();

		if ($this->tokens->valid() === true)
		{
			$token = $this->tokens->current();

			if ($token[0] === '(')
			{
				$this->currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));

				$stack = 1;

				while ($stack > 0 && $this->tokens->valid() === true)
				{
					$this->tokens->next();

					if ($this->tokens->valid() === true)
					{
						$token = $this->tokens->current();

						if ($token[0] === '(')
						{
							$stack++;
						}
						else if ($token[0] === ')')
						{
							$stack--;
						}

						$this->currentIterator->append(new tokenizer\token($token[0], isset($token[1]) === false ? null : $token[1], isset($token[2]) === false ? null : $token[2]));
					}
				}
			}
		}
	}

	private function nextTokenIs($tokenName, array $skipedTags = array(T_WHITESPACE, T_COMMENT, T_INLINE_HTML))
	{
		$key = $this->tokens->key() + 1;

		while (isset($this->tokens[$key]) === true && in_array($this->tokens[$key], $skipedTags) === true)
		{
			$key++;
		}

		$key++;

		return (isset($this->tokens[$key]) === true && $this->tokens[$key][0] === $tokenName);
	}
}

?>