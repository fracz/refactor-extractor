<?php namespace Laravel\Validation;

use Closure;
use Laravel\IoC;
use Laravel\Str;
use Laravel\Lang_Factory;

class Validator_Factory {

	/**
	 * The language factory instance.
	 *
	 * @var Lang_Factory
	 */
	protected $lang;

	/**
	 * The registered custom validators.
	 *
	 * @var array
	 */
	protected $validators = array();

	/**
	 * Create a new validator factory instance.
	 *
	 * @param  Lang_Factory  $lang
	 * @return void
	 */
	public function __construct(Lang_Factory $lang)
	{
		$this->lang = $lang;
	}

	/**
	 * Create a new validator instance.
	 *
	 * @param  array      $attributes
	 * @param  array      $rules
	 * @param  array      $messages
	 * @return Validator
	 */
	public function make($attributes, $rules, $messages = array())
	{
		return new Validator($this->lang, $this->validators, $attributes, $rules, $messages);
	}

	/**
	 * Register a custom validation callback.
	 *
	 * @param  string     $name
	 * @param  string     $message
	 * @param  Closure    $closure
	 * @return Validator
	 */
	public function register($name, $message, Closure $closure)
	{
		$this->validators[$name] = compact('message', 'closure');

		return $this;
	}

}

class Validator {

	/**
	 * The registered custom validators.
	 *
	 * @var array
	 */
	protected $validators = array();

	/**
	 * The validation rules.
	 *
	 * @var array
	 */
	protected $rules = array();

	/**
	 * The validation messages.
	 *
	 * @var array
	 */
	protected $messages = array();

	/**
	 * The language that should be used when retrieving error messages.
	 *
	 * @var string
	 */
	protected $language;

	/**
	 * The size related validation rules.
	 *
	 * @var array
	 */
	protected $size_rules = array('size', 'between', 'min', 'max');

	/**
	 * The numeric related validation rules.
	 *
	 * @var array
	 */
	protected $numeric_rules = array('numeric', 'integer');

	/**
	 * The database connection that should be used by the validator.
	 *
	 * @var DB\Connection
	 */
	public $connection;

	/**
	 * The array being validated.
	 *
	 * @var array
	 */
	public $attributes;

	/**
	 * The post-validation error messages.
	 *
	 * @var Messages
	 */
	public $errors;

	/**
	 * Create a new validator instance.
	 *
	 * @param  Lang_Factory  $lang
	 * @param  array         $validators
	 * @param  array         $attributes
	 * @param  array         $rules
	 * @param  array         $messages
	 * @return void
	 */
	public function __construct(Lang_Factory $lang, $validators, $attributes, $rules, $messages = array())
	{
		foreach ($rules as $key => &$rule)
		{
			$rule = (is_string($rule)) ? explode('|', $rule) : $rule;
		}

		// Register all of the custom validators and their corresponding error messages.
		// The validators are executed via the magic __call method. The validator names
		// are prefixed with "validate_" to match the built-in validators.
		foreach ($validators as $key => $value)
		{
			$this->messages[$key] = $value['message'];

			$this->validators['validate_'.$key] = $value['closure'];
		}

		$this->lang = $lang;
		$this->rules = $rules;
		$this->attributes = $attributes;
		$this->messages = array_merge($this->messages, $messages);
	}

	/**
	 * Validate the target array using the specified validation rules.
	 *
	 * @return bool
	 */
	public function invalid()
	{
		return ! $this->valid();
	}

	/**
	 * Validate the target array using the specified validation rules.
	 *
	 * @return bool
	 */
	public function valid()
	{
		$this->errors = new Messages;

		foreach ($this->rules as $attribute => $rules)
		{
			foreach ($rules as $rule)
			{
				$this->check($attribute, $rule);
			}
		}

		return count($this->errors->messages) == 0;
	}

	/**
	 * Evaluate an attribute against a validation rule.
	 *
	 * @param  string  $attribute
	 * @param  string  $rule
	 * @return void
	 */
	protected function check($attribute, $rule)
	{
		list($rule, $parameters) = $this->parse($rule);

		if ( ! method_exists($this, $validator = 'validate_'.$rule) and ! isset($this->validators[$validator]))
		{
			throw new \Exception("Validation rule [$rule] doesn't exist.");
		}

		// No validation will be run for attributes that do not exist unless the rule being validated
		// is "required" or "accepted". No other rules have implicit "required" checks.
		if ( ! $this->validate_required($attribute) and ! in_array($rule, array('required', 'accepted'))) return;

		if ( ! $this->$validator($attribute, $parameters, $this))
		{
			$message = $this->format_message($this->get_message($attribute, $rule), $attribute, $rule, $parameters);

			$this->errors->add($attribute, $message, $attribute, $rule, $parameters);
		}
	}

	/**
	 * Validate that a required attribute exists in the attributes array.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_required($attribute)
	{
		if ( ! array_key_exists($attribute, $this->attributes)) return false;

		if (is_string($this->attributes[$attribute]) and trim($this->attributes[$attribute]) === '') return false;

		return true;
	}

	/**
	 * Validate that an attribute has a matching confirmation attribute.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_confirmed($attribute)
	{
		$value = $this->attributes[$attribute];

		$confirmation = $this->attributes[$attribute.'_confirmation'];

		return array_key_exists($attribute.'_confirmation', $this->attributes) and $value == $confirmation;
	}

	/**
	 * Validate that an attribute was "accepted".
	 *
	 * This validation rule implies the attribute is "required".
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_accepted($attribute)
	{
		$value = $this->attributes[$attribute];

		return $this->validate_required($attribute) and ($value == 'yes' or $value == '1');
	}

	/**
	 * Validate that an attribute is numeric.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_numeric($attribute)
	{
		return is_numeric($this->attributes[$attribute]);
	}

	/**
	 * Validate that an attribute is an integer.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_integer($attribute)
	{
		return filter_var($this->attributes[$attribute], FILTER_VALIDATE_INT) !== false;
	}

	/**
	 * Validate the size of an attribute.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_size($attribute, $parameters)
	{
		return $this->get_size($attribute) == $parameters[0];
	}

	/**
	 * Validate the size of an attribute is between a set of values.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_between($attribute, $parameters)
	{
		return $this->get_size($attribute) >= $parameters[0] and $this->get_size($attribute) <= $parameters[1];
	}

	/**
	 * Validate the size of an attribute is greater than a minimum value.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_min($attribute, $parameters)
	{
		return $this->get_size($attribute) >= $parameters[0];
	}

	/**
	 * Validate the size of an attribute is less than a maximum value.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_max($attribute, $parameters)
	{
		return $this->get_size($attribute) <= $parameters[0];
	}

	/**
	 * Get the size of an attribute.
	 *
	 * @param  string  $attribute
	 * @return mixed
	 */
	protected function get_size($attribute)
	{
		if (is_numeric($this->attributes[$attribute]) and $this->has_rule($attribute, $this->numeric_rules))
		{
			return $this->attributes[$attribute];
		}

		$value = $this->attributes[$attribute];

		return (array_key_exists($attribute, $_FILES)) ? $value['size'] / 1024 : Str::length(trim($value));
	}

	/**
	 * Validate an attribute is contained within a list of values.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_in($attribute, $parameters)
	{
		return in_array($this->attributes[$attribute], $parameters);
	}

	/**
	 * Validate an attribute is not contained within a list of values.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_not_in($attribute, $parameters)
	{
		return ! in_array($this->attributes[$attribute], $parameters);
	}

	/**
	 * Validate the uniqueness of an attribute value on a given database table.
	 *
	 * If a database column is not specified, the attribute name will be used.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_unique($attribute, $parameters)
	{
		if ( ! isset($parameters[1])) $parameters[1] = $attribute;

		if (is_null($this->connection)) $this->connection = IoC::resolve('laravel.database')->connection();

		return $this->connection->table($parameters[0])->where($parameters[1], '=', $this->attributes[$attribute])->count() == 0;
	}

	/**
	 * Validate than an attribute is a valid e-mail address.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_email($attribute)
	{
		return filter_var($this->attributes[$attribute], FILTER_VALIDATE_EMAIL) !== false;
	}

	/**
	 * Validate than an attribute is a valid URL.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_url($attribute)
	{
		return filter_var($this->attributes[$attribute], FILTER_VALIDATE_URL) !== false;
	}

	/**
	 * Validate that an attribute is an active URL.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_active_url($attribute)
	{
		$url = str_replace(array('http://', 'https://', 'ftp://'), '', Str::lower($this->attributes[$attribute]));

		return checkdnsrr($url);
	}

	/**
	 * Validate the MIME type of a file is an image MIME type.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_image($attribute)
	{
		return $this->validate_mimes($attribute, array('jpg', 'png', 'gif', 'bmp'));
	}

	/**
	 * Validate than an attribute contains only alphabetic characters.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_alpha($attribute)
	{
		return preg_match('/^([a-z])+$/i', $this->attributes[$attribute]);
	}

	/**
	 * Validate than an attribute contains only alpha-numeric characters.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_alpha_num($attribute)
	{
		return preg_match('/^([a-z0-9])+$/i', $this->attributes[$attribute]);
	}

	/**
	 * Validate than an attribute contains only alpha-numeric characters, dashes, and underscores.
	 *
	 * @param  string  $attribute
	 * @return bool
	 */
	protected function validate_alpha_dash($attribute)
	{
		return preg_match('/^([-a-z0-9_-])+$/i', $this->attributes[$attribute]);
	}

	/**
	 * Validate the MIME type of a file upload attribute is in a set of MIME types.
	 *
	 * @param  string  $attribute
	 * @param  array   $parameters
	 * @return bool
	 */
	protected function validate_mimes($attribute, $parameters)
	{
		$file = IoC::container()->resolve('laravel.file');

		foreach ($parameters as $extension)
		{
			if ($file->is($extension, $this->attributes[$attribute]['tmp_name'])) return true;
		}

		return false;
	}

	/**
	 * Get the proper error message for an attribute and rule.
	 *
	 * Developer specified attribute specific rules take first priority.
	 * Developer specified error rules take second priority.
	 *
	 * If the message has not been specified by the developer, the default will be used
	 * from the validation language file.
	 *
	 * @param  string  $attribute
	 * @param  string  $rule
	 * @return string
	 */
	protected function get_message($attribute, $rule)
	{
		if (array_key_exists($attribute.'_'.$rule, $this->messages))
		{
			return $this->messages[$attribute.'_'.$rule];
		}
		elseif (array_key_exists($rule, $this->messages))
		{
			return $this->messages[$rule];
		}
		else
		{
			$message = $this->lang->line('validation.'.$rule)->get($this->language);

			// For "size" rules that are validating strings or files, we need to adjust
			// the default error message for the appropriate units.
			if (in_array($rule, $this->size_rules) and ! $this->has_rule($attribute, $this->numeric_rules))
			{
				return (array_key_exists($attribute, IoC::container()->resolve('laravel.input')->files()))
                                   ? rtrim($message, '.').' '.$this->lang->line('validation.kilobytes')->get($this->language).'.'
                                   : rtrim($message, '.').' '.$this->lang->line('validation.characters')->get($this->language).'.';
			}

			return $message;
		}
	}

	/**
	 * Replace all error message place-holders with actual values.
	 *
	 * @param  string  $message
	 * @param  string  $attribute
	 * @param  string  $rule
	 * @param  array   $parameters
	 * @return string
	 */
	protected function format_message($message, $attribute, $rule, $parameters)
	{
		$display = $this->lang->line('attributes.'.$attribute)->get($this->language, str_replace('_', ' ', $attribute));

		$message = str_replace(':attribute', $display, $message);

		if (in_array($rule, $this->size_rules))
		{
			$max = ($rule == 'between') ? $parameters[1] : $parameters[0];

			$message = str_replace(array(':size', ':min', ':max'), array($parameters[0], $parameters[0], $max), $message);
		}
		elseif (in_array($rule, array('in', 'not_in', 'mimes')))
		{
			$message = str_replace(':values', implode(', ', $parameters), $message);
		}

		return $message;
	}

	/**
	 * Determine if an attribute has a rule assigned to it.
	 *
	 * @param  string  $attribute
	 * @param  array   $rules
	 * @return bool
	 */
	protected function has_rule($attribute, $rules)
	{
		foreach ($this->rules[$attribute] as $rule)
		{
			list($rule, $parameters) = $this->parse($rule);

			if (in_array($rule, $rules)) return true;
		}

		return false;
	}

	/**
	 * Extract the rule name and parameters from a rule.
	 *
	 * @param  string  $rule
	 * @return array
	 */
	protected function parse($rule)
	{
		$parameters = (($colon = strpos($rule, ':')) !== false) ? explode(',', substr($rule, $colon + 1)) : array();

		return array(is_numeric($colon) ? substr($rule, 0, $colon) : $rule, $parameters);
	}

	/**
	 * Set the language that should be used when retrieving error messages.
	 *
	 * @param  string     $language
	 * @return Validator
	 */
	public function lang($language)
	{
		$this->language = $language;
		return $this;
	}

	/**
	 * Set the database connection that should be used by the validator.
	 *
	 * @param  Database\Connection  $connection
	 * @return Validator
	 */
	public function connection(Database\Connection $connection)
	{
		$this->connection = $connection;
		return $this;
	}

	/**
	 * Magic Method for calling custom registered validators.
	 */
	public function __call($method, $parameters)
	{
		return call_user_func_array($this->validators[$method], $parameters);
	}

}