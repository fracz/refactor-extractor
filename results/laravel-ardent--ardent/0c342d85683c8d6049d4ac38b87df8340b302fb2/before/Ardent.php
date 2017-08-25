<?php namespace LaravelBook\Ardent;

/*
 * This file is part of the Ardent package.
 *
 * (c) Max Ehsan <contact@laravelbook.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

use Closure;
use Illuminate\Container\Container;
use Illuminate\Database\Capsule\Manager as DatabaseCapsule;
use Illuminate\Events\Dispatcher;
use Illuminate\Support\MessageBag;
use Illuminate\Support\Facades\Input;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Str;
use Illuminate\Validation\Factory as ValidationFactory;
use Symfony\Component\Translation\Loader\PhpFileLoader;
use Symfony\Component\Translation\Translator;

/**
 * Ardent - Self-validating Eloquent model base class
 *
 */
abstract class Ardent extends Model {

    /**
     * The rules to be applied to the data.
     *
     * @var array
     */
    public static $rules = array();

    /**
     * The array of custom error messages.
     *
     * @var array
     */
    public static $customMessages = array();

    /**
     * The message bag instance containing validation error messages
     *
     * @var \Illuminate\Support\MessageBag
     */
    public $validationErrors;

    /**
     * Makes the validation procedure throw an {@link InvalidModelException} instead of returning
     * false when validation fails.
     *
     * @var bool
     */
    public $throwOnValidation = false;

    /**
     * Forces the behavior of findOrFail in very find method - throwing a {@link ModelNotFoundException}
     * when the model is not found.
     *
     * @var bool
     */
    public static $throwOnFind = false;

    /**
     * If set to true, the object will automatically populate model attributes from Input::all()
     *
     * @var bool
     */
    public $autoHydrateEntityFromInput = false;

    /**
     * By default, Ardent will attempt hydration only if the model object contains no attributes and
     * the $autoHydrateEntityFromInput property is set to true.
     * Setting $forceEntityHydrationFromInput to true will bypass the above check and enforce
     * hydration of model attributes.
     *
     * @var bool
     */
    public $forceEntityHydrationFromInput = false;

    /**
     * If set to true, the object will automatically remove redundant model
     * attributes (i.e. confirmation fields).
     *
     * @var bool
     */
    public $autoPurgeRedundantAttributes = false;

    /**
     * Array of closure functions which determine if a given attribute is deemed
     * redundant (and should not be persisted in the database)
     *
     * @var array
     */
    protected $purgeFilters = array();

    protected $purgeFiltersInitialized = false;

    /**
     * List of attribute names which should be hashed using the Bcrypt hashing algorithm.
     *
     * @var array
     */
    public static $passwordAttributes = array();

    /**
     * If set to true, the model will automatically replace all plain-text passwords
     * attributes (listed in $passwordAttributes) with hash checksums
     *
     * @var bool
     */
    public $autoHashPasswordAttributes = false;

    /**
     * If set to true will try to instantiate the validator as if it was outside Laravel.
     *
     * @var bool
     */
    protected static $externalValidator = false;

    /**
     * A Translator instance, to be used by standalone Ardent instances.
     *
     * @var \Illuminate\Validation\Factory
     */
    protected static $validationFactory;

    /**
     * Can be used to ease declaration of relationships in Ardent models.
     * Follows closely the behavior of the relation methods used by Eloquent, but packing them into an indexed array
     * with relation constants make the code less cluttered.
     *
     * It should be declared with camel-cased keys as the relation name, and value being a mixed array with the
     * relation constant being the first (0) value, the second (1) being the classname and the next ones (optionals)
     * having named keys indicating the other arguments of the original methods: 'foreignKey' (belongsTo, hasOne,
     * belongsToMany and hasMany); 'table' and 'otherKey' (belongsToMany only); 'name', 'type' and 'id' (specific for
     * morphTo, morphOne and morphMany).
     * Exceptionally, the relation type MORPH_TO does not include a classname, following the method declaration of
     * {@link \Illuminate\Database\Eloquent\Model::morphTo}.
     *
     * Example:
     * <code>
     * class Order extends Ardent {
     *     protected static $relations = array(
     *         'items'    => array(self::HAS_MANY, 'Item'),
     *         'owner'    => array(self::HAS_ONE, 'User', 'foreignKey' => 'user_id'),
     *         'pictures' => array(self::MORPH_MANY, 'Picture', 'name' => 'imageable')
     *     );
     * }
     * </code>
     *
     * @see \Illuminate\Database\Eloquent\Model::hasOne
     * @see \Illuminate\Database\Eloquent\Model::hasMany
     * @see \Illuminate\Database\Eloquent\Model::belongsTo
     * @see \Illuminate\Database\Eloquent\Model::belongsToMany
     * @see \Illuminate\Database\Eloquent\Model::morphTo
     * @see \Illuminate\Database\Eloquent\Model::morphOne
     * @see \Illuminate\Database\Eloquent\Model::morphMany
     *
     * @var array
     */
    protected static $relationsData = array();

    const HAS_ONE = 'hasOne';

    const HAS_MANY = 'hasMany';

    const BELONGS_TO = 'belongsTo';

    const BELONGS_TO_MANY = 'belongsToMany';

    const MORPH_TO = 'morphTo';

    const MORPH_ONE = 'morphOne';

    const MORPH_MANY = 'morphMany';

    /**
     * Array of relations used to verify arguments used in the {@link $relationsData}
     *
     * @var array
     */
    protected static $relationTypes = array(
        self::HAS_ONE, self::HAS_MANY,
        self::BELONGS_TO, self::BELONGS_TO_MANY,
        self::MORPH_TO, self::MORPH_ONE, self::MORPH_MANY
    );

    /**
     * Create a new Ardent model instance.
     *
     * @param array $attributes
     * @return \LaravelBook\Ardent\Ardent
     */
    public function __construct(array $attributes = array()) {

        parent::__construct($attributes);
        $this->validationErrors = new MessageBag;
    }

    /**
     * Looks for the relation in the {@link $relationsData} array and does the correct magic as Eloquent would require
     * inside relation methods. For more information, read the documentation of the mentioned property.
     *
     * @param $relationName the relation key, camel-case version
     * @return \Illuminate\Database\Eloquent\Relations\Relation
     * @throws \InvalidArgumentException when the first param of the relation is not a relation type constant,
     * or there's one or more arguments missing
     * @see Ardent::relationsData
     */
    protected function handleRelationalArray($relationName) {
        $relation     = static::$relationsData[$relationName];
        $relationType = $relation[0];
        $errorHeader = "Relation '$relationName' on model '".get_called_class();

        if (!in_array($relationType, static::$relationTypes)) {
            throw new \InvalidArgumentException($errorHeader.' should have as first param one of the relation constants of the Ardent class.');
        }
        if (!isset($relation[1]) && $relationType != self::MORPH_TO) {
            throw new \InvalidArgumentException($errorHeader.' should have at least two params: relation type and classname.');
        }
        if (isset($relation[1]) && $relationType == self::MORPH_TO) {
            throw new \InvalidArgumentException($errorHeader.' is a morphTo relation and should not contain additional arguments.');
        }

        $verifyArgs = function (array $optional, array $required = array()) use ($relationName, &$relation, $errorHeader) {
            $missing = array('required' => array(), 'optional' => array());

            foreach(array('required', 'optional') as $keyType) {
                foreach ($$keyType as $key) {
                    if (!array_key_exists($key, $relation)) {
                        $missing[$keyType][] = $key;
                    }
                }
            }

            if ($missing['required']) {
                throw new \InvalidArgumentException($errorHeader.'
                    should contain the following key(s): '.join(', ', $missing['required']));
            }
            if ($missing['optional']) {
                foreach ($missing['optional'] as $include) {
                    $relation[$include] = null;
                }
            }
        };

        switch ($relationType) {
            case self::HAS_ONE:
            case self::HAS_MANY:
            case self::BELONGS_TO:
                $verifyArgs(array('foreignKey'));
                return $this->$relationType($relation[1], $relation['foreignKey']);

            case self::BELONGS_TO_MANY:
                $verifyArgs(array('table', 'foreignKey', 'otherKey'));
                return $this->$relationType($relation[1], $relation['table'], $relation['foreignKey'],
                    $relation['otherKey']);

            case self::MORPH_TO:
                $verifyArgs(array('name', 'type', 'id'));
                return $this->$relationType($relation['name'], $relation['type'], $relation['id']);

            case self::MORPH_ONE:
            case self::MORPH_MANY:
                $verifyArgs(array('type', 'id'), array('name'));
                return $this->$relationType($relation[1], $relation['name'], $relation['type'], $relation['id']);
        }
    }


    /**
     * Handle dynamic method calls into the method.
     * Overrided from {@link Eloquent} to implement recognition of the {@link $relationsData} array.
     *
     * @param  string  $method
     * @param  array   $parameters
     * @return mixed
     */
    public function __call($method, $parameters) {
        if (array_key_exists($method, static::$relationsData)) {
            return $this->handleRelationalArray($method);
        }

        return parent::__call($method, $parameters);
    }


    /**
     * Get an attribute from the model.
     * Overrided from {@link Eloquent} to implement recognition of the {@link $relationsData} array.
     *
     * @param  string  $key
     * @return mixed
     */
    public function getAttribute($key) {
        $attr = parent::getAttribute($key);

        if ($attr === null) {
            $camelKey = camel_case($key);
            if (array_key_exists($camelKey, static::$relationsData)) {
                $this->relations[$key] = $this->$camelKey()->getResults();
                return $this->relations[$key];
            }
        }

        return $attr;
    }

    /**
     * Configures Ardent to be used outside of Laravel - correctly setting Eloquent and Validation modules.
     * @todo Should allow for additional language files. Would probably receive a Translator instance as an optional argument, or a list of translation files.
     *
     * @param array $connection Connection info used by {@link \Illuminate\Database\Capsule\Manager::addConnection}.
     * Should contain driver, host, port, database, username, password, charset and collation.
     */
    public static function configureAsExternal(array $connection) {
        $db = new DatabaseCapsule;
        $db->addConnection($connection);
        $db->setEventDispatcher(new Dispatcher(new Container));
        //TODO: configure a cache manager (as an option)
        $db->bootEloquent();

        $translator = new Translator('en');
        $translator->addLoader('file_loader', new PhpFileLoader());
        $translator->addResource('file_loader',
            dirname(__FILE__).DIRECTORY_SEPARATOR.'..'.DIRECTORY_SEPARATOR.'lang'.DIRECTORY_SEPARATOR.'en'.
            DIRECTORY_SEPARATOR.'validation.php', 'en');

        self::$externalValidator = true;
        self::$validationFactory = new ValidationFactory($translator);
    }

    protected static function makeValidator($data, $rules, $customMessages) {
        if (self::$externalValidator) {
            return self::$validationFactory->make($data, $rules, $customMessages);
        } else {
            return Validator::make($data, $rules, $customMessages);
        }
    }

    /**
     * Validate the model instance
     *
     * @param array $rules          Validation rules
     * @param array $customMessages Custom error messages
     * @return bool
     * @throws InvalidModelException
     */
    public function validate(array $rules = array(), array $customMessages = array()) {

        if ($this->fireModelEvent('validating') === false) {
            if ($this->throwOnValidation) {
                throw new InvalidModelException($this);
            } else {
                return false;
            }
        }

        // check for overrides, then remove any empty rules
        $rules = (empty($rules))? static::$rules : $rules;
        foreach ($rules as $field => $rls) {
            if ($rls == '') {
                unset($rules[$field]);
            }
        }

        if (empty($rules)) {
            return true;
        }

        $customMessages = (empty($customMessages))? static::$customMessages : $customMessages;

        if ($this->forceEntityHydrationFromInput || (empty($this->attributes) && $this->autoHydrateEntityFromInput)) {
            // pluck only the fields which are defined in the validation rule-set
            $attributes = array_intersect_key(Input::all(), $rules);

            //Set each given attribute on the model
            foreach ($attributes as $key => $value) {
                $this->setAttribute($key, $value);
            }
        }

        $data = $this->getAttributes(); // the data under validation

        // perform validation
        $validator = self::makeValidator($data, $rules, $customMessages);
        $success   = $validator->passes();

        if ($success) {
            // if the model is valid, unset old errors
            if ($this->validationErrors->count() > 0) {
                $this->validationErrors = new MessageBag;
            }
        } else {
            // otherwise set the new ones
            $this->validationErrors = $validator->messages();

            // stash the input to the current session
            if (!self::$externalValidator && Input::hasSessionStore()) {
                Input::flash();
            }

            if ($this->throwOnValidation) {
                throw new InvalidModelException($this);
            }
        }

        $this->fireModelEvent('validated', false);

        return $success;
    }

    /**
     * Invoked before a model is saved. Return false to abort the operation.
     *
     * @param bool $forced Indicates whether the model is being saved forcefully
     * @return bool
     */
    protected function beforeSave($forced = false) {
        return true;
    }

    /**
     * Called after a model is successfully saved.
     *
     * @param bool $success Indicates whether the database save operation succeeded
     * @param bool $forced  Indicates whether the model is being saved forcefully
     *
     * @return void
     */
    protected function afterSave($success, $forced = false) {
        //
    }

    /**
     * Save the model to the database.
     *
     * @param array   $rules
     * @param array   $customMessages
     * @param array   $options
     * @param Closure $beforeSave
     * @param Closure $afterSave
     * @return bool
     */
    public function save(array $rules = array(),
        array $customMessages = array(),
        array $options = array(),
        Closure $beforeSave = null,
        Closure $afterSave = null) {

        // validate
        $validated = $this->validate($rules, $customMessages);

        // execute beforeSave callback
        $proceed = is_null($beforeSave)? $this->beforeSave(false) : $beforeSave($this);

        // attempt to save if all conditions are satisfied
        $success = ($proceed && $validated)? $this->performSave($options) : false;

        is_null($afterSave)? $this->afterSave($success, false) : $afterSave($this);

        return $success;
    }

    /**
     * Force save the model even if validation fails.
     *
     * @param array   $rules         :array
     * @param array   $customMessages:array
     * @param array   $options
     * @param Closure $beforeSave
     * @param Closure $afterSave
     * @return bool
     */
    public function forceSave(array $rules = array(),
        array $customMessages = array(),
        array $options = array(),
        Closure $beforeSave = null,
        Closure $afterSave = null) {

        // validate the model
        $this->validate($rules, $customMessages);

        // execute beforeForceSave callback
        $proceed = is_null($beforeSave)? $this->beforeSave(true) : $beforeSave($this);

        // attempt to save regardless of the outcome of validation
        $success = $proceed? $this->performSave($options) : false;

        is_null($afterSave)? $this->afterSave($success, true) : $afterSave($this);

        return $success;
    }

    /**
     * Add the basic purge filters
     *
     * @return void
     */
    protected function addBasicPurgeFilters() {
        if ($this->purgeFiltersInitialized) {
            return;
        }

        $this->purgeFilters[] = function ($attributeKey) {
            // disallow password confirmation fields
            if (Str::endsWith($attributeKey, '_confirmation')) {
                return false;
            }

            // "_method" is used by Illuminate\Routing\Router to simulate custom HTTP verbs
            if (strcmp($attributeKey, '_method') === 0) {
                return false;
            }

            return true;
        };

        $this->purgeFiltersInitialized = true;
    }

    /**
     * Removes redundant attributes from model
     *
     * @param array $array Input array
     * @return array
     */
    protected function purgeArray(array $array = array()) {

        $result = array();
        $keys   = array_keys($array);

        $this->addBasicPurgeFilters();

        if (!empty($keys) && !empty($this->purgeFilters)) {
            foreach ($keys as $key) {
                $allowed = true;

                foreach ($this->purgeFilters as $filter) {
                    $allowed = $filter($key);

                    if (!$allowed) {
                        break;
                    }
                }

                if ($allowed) {
                    $result[$key] = $array[$key];
                }
            }
        }

        return $result;
    }

    /**
     * Saves the model instance to database. If necessary, it will purge the model attributes
     * of unnecessary fields. It will also replace plain-text password fields with their hashes.
     *
     * @param array $options
     * @return bool
     */
    protected function performSave(array $options) {

        if ($this->autoPurgeRedundantAttributes) {
            $this->attributes = $this->purgeArray($this->getAttributes());
        }

        if ($this->autoHashPasswordAttributes) {
            $this->attributes = $this->hashPasswordAttributes($this->getAttributes(), static::$passwordAttributes);
        }

        return parent::save($options);
    }

    /**
     * Get validation error message collection for the Model
     *
     * @return \Illuminate\Support\MessageBag
     */
    public function errors() {
        return $this->validationErrors;
    }

    /**
     * Automatically replaces all plain-text password attributes (listed in $passwordAttributes)
     * with hash checksum.
     *
     * @param array $attributes
     * @param array $passwordAttributes
     * @return array
     */
    protected function hashPasswordAttributes(array $attributes = array(), array $passwordAttributes = array()) {

        if (empty($passwordAttributes) || empty($attributes)) {
            return $attributes;
        }

        $result = array();
        foreach ($attributes as $key => $value) {

            if (in_array($key, $passwordAttributes) && !is_null($value)) {
                if ($value != $this->getOriginal($key)) {
                    $result[$key] = Hash::make($value);
                }
            } else {
                $result[$key] = $value;
            }
        }

        return $result;
    }

    /**
     * When given an ID and a Laravel validation rules array, this function
     * appends the ID to the 'unique' rules given. The resulting array can
     * then be fed to a Ardent save so that unchanged values
     * don't flag a validation issue. Rules can be in either strings
     * with pipes or arrays, but the returned rules are in arrays.
     *
     * @param int   $id
     * @param array $rules
     *
     * @return array Rules with exclusions applied
     */
    protected function buildUniqueExclusionRules() {
        // Because Ardent uses statics (sigh), we need to do this to get the
        // model's rules.
        $class = new \ReflectionClass($this);
        $rules = $class->getStaticPropertyValue('rules');

        foreach ($rules as $field => &$ruleset) {
            // If $ruleset is a pipe-separated string, switch it to array
            $ruleset = (is_string($ruleset))? explode('|', $ruleset) : $ruleset;

            foreach ($ruleset as &$rule) {
                if (strpos($rule, 'unique') === 0) {
                    $params = explode(',', $rule);

                    // Append field name if needed
                    if (count($params) == 1) {
                        $params[2] = $field;
                    }

                    $params[3] = $this->id;

                    $rule = implode(',', $params);
                }
            }
        }

        return $rules;
    }

    /**
     * Update a model already saved in the database.
     *
     * @param array   $rules
     * @param array   $customMessages
     * @param array   $options
     * @param Closure $beforeSave
     * @param Closure $afterSave
     * @return bool
     */
    public function update(array $rules = array(),
        array $customMessages = array(),
        array $options = array(),
        Closure $beforeSave = null,
        Closure $afterSave = null) {

        // Only automatically modify rules if there are none coming in
        if (count($rules == 0)) {
            $rules = $this->buildUniqueExclusionRules();
        }

        return $this->save($rules, $customMessages, $options, $beforeSave, $afterSave);
    }

    /**
     * Find a model by its primary key.
     * If {@link $throwOnFind} is set, will use {@link findOrFail} internally.
     *
     * @param  mixed $id
     * @param  array $columns
     * @return Ardent|Collection
     */
    public static function find($id, $columns = array('*')) {
        if (static::$throwOnFind && debug_backtrace()[1]['function'] != 'findOrFail') {
            return self::findOrFail($id, $columns);
        } else {
            return parent::find($id, $columns);
        }
    }
}