<?php


/**
 * Our base class for all Data Type plugins. All Data Types must define a class that extends this class.
 * This page documents and defines (where the language permits!) what's required, what's optional, and
 * what each method and member variable does.
 */
abstract class DataTypePlugin {


	// REQUIRED MEMBER VARS

	protected $dataTypeName = "";
	protected $hasHelpDialog; // boolean
	protected $dataTypeFieldGroup; // string
	protected $dataTypeFieldGroupOrder; // int
	protected $processOrder = 1; // int


	// OPTIONAL MEMBER VARS

	/**
	 * An array of JS modules that need to be included for this module. They should be requireJS-friendly
	 * modules.
	 * @var array
	 */
	protected $jsModules = array();

	/**
	 * A single CSS file for any additional CSS needed for the module. It's up to the developer to properly
	 * name their CSS classes/IDs to prevent namespace collisions.
	 */
	protected $cssFile = "";

	/**
	 * Contains all strings for the current language. This is populated automatically on instantiation and
	 * contains the strings for the currently selected language.
	 * @var array
	 */
	public $L = array();


	// REQUIRED METHODS

	/**
	 * This is the main workhorse function: it does the work of actually generating a random data snippet.
	 *
	 * @param integer $rowNum the row number in the generated content (indexed from 1)
	 * @param mixed $options whatever options were passed for this Data Type, i.e. whatever information was returned
	 *   - and in whatever format - by getRowGenerationOptions(). By default, this is set to null.
	 * @param array $existingRowData depending on the Data Type's processOrder, this will contain all the data from
	 *   fields that have already been processed.
	 * @return string/int/primitive
	 */
	abstract function generate($rowNum, $options, $existingRowData);

	/**
	 * @param string $export_type e.g. "sql"
	 * @param mixed $options e.g. "mysql" or "oracle"
	 * @return string
	 */
	abstract function getExportTypeInfo($exportType, $options);


	// 2. OPTIONALLY DEFINED FUNCTIONS

	/**
	 * The default constructor. Automatically populates the $L member var with whatever language is currently being
	 * used.
	 *
	 * @param string $content "ui" / "generation". Data Types are instantiated in one of two contexts: once when
	 *    the main UI page loads, so that the Data Type can be presented as an option for selection in the Data
	 *    Generator, and secondly when we're actually actually generating the results. Data Types can choose
	 */
	public function __construct($runtimeContext) {

		// a little magic to find the current instantiated class's folder
		$currClass = new ReflectionClass(get_class($this));
		$currClassFolder = dirname($currClass->getFileName());

		$defaultLangFileStr = Core::getDefaultLanguageFile();
		$currentLangFileStr = Core::$language->getCurrentLanguageFile();

		$currentLangFile = $currClassFolder . "/lang/" . $currentLangFileStr . ".php";
		$defaultLangFile = $currClassFolder . "/lang/" . $defaultLangFileStr . ".php";

		if (file_exists($currentLangFile)) {
			require($currentLangFile);
		} else if (file_exists($defaultLangFile)) {
			require($defaultLangFile);
		}

		if (isset($L)) {
			$this->L = $L;
		}
	}


	/**
	 * This is called once during the initial installation of the script, or when the installation is reset (which is
	 * effectively a fresh install). It is called AFTER the Core tables are installed, and you can rely
	 * on Core::$db having been initialized and the database connection having been set up.
	 *
	 * @return array [0] success / error
	 * 				 [1] the error message, if there was a problem
	 */
	static function install() {
		return array(true, "");
	}

	/**
	 * If the Data Type wants to include something in the Example column, it should return the raw HTML via this function.
	 * If this function isn't defined (or it returns an empty string), the string "No examples available." will be
	 * outputted in the cell. This is used for inserting static content into the appropriate spot in the table; if the
	 * Data Type needs something more dynamic, it should subscribe to the appropriate event.
	 */
	public function getExampleColumnHTML() {
		return "";
	}

	/**
	 * If the Data Type wants to include something in the Options column, it must return the HTML via this function.
	 * If this function isn't defined (or it returns an empty string), the string "No options available." will be
	 * outputted in the cell. This is used for inserting static content into the appropriate spot in the table; if the
	 * Data Type needs something more dynamic, it should subscribe to the appropriate event.
	 */
	public function getOptionsColumnHTML() {
		return "";
	}

	/**
	 * Called during data generation. This determines what options the user selected in the user interface; it's
	 * used to figure out what settings to pass to each Data Type to provide that function the information needed
	 * to generate that particular data item.
	 *
	 * Note: if this function determines that the values entered by the user in the options column are invalid
	 * (most likely just incomplete) the function can explicitly return false to tell the core script to ignore
	 * this row.
	 *
	 * @param array $post the entire contents of $_POST
	 * @param integer the column number (well, *row* in the UI!) of the item
	 * @param integer the number of columns in the data set
	 * @return mixed
	 *        - false, if the Data Type doesn't have sufficient information to generate the row (i.e. things weren't
	 *        filled in in the UI and the Data Type didn't add proper validation)
	 *        - anything else. This can be any data structure needed by the Data Type. It'll be passed as-is
	 *        into the generateItem function as the second parameter.
	 */
	public function getRowGenerationOptions($postdata, $column, $numCols) {
		return null;
	}

	/**
	 * Returns information about the help dialog for this Data Type. It returns a hash with two keys:
	 *    [dialogWidth]
	 *    [content]
	 *
	 * [shouldn't be required... just like install(), but I'd like to mention it in this file for documentation purposes]
	 *
	 */
	public function getHelpDialogInfo() {
		return;
	}

	/**
	 * For debugging and dev work.
	 */
	public function __toString() {
		echo $this->getName();
	}


	// 3. NON-OVERRIDABLE FUNCTIONS
	// - these are automatically inherited by all Data Types when they extend this abstract class.


	// TODO should return in current language...
	final public function getName() {
		return $this->dataTypeName;
	}

	/**
	 * Returns true if this Data Type has a help dialog.
	 *
	 * @return boolean
	 */
	final public function hasHelpDialog() {
		return $this->hasHelpDialog;
	}

	/**
	 * Returns an array of file names, which will be included ONCE in the main generator page.
	 *
	 * @return array
	 */
	final public function getIncludedFiles() {
		return $this->includedFiles;
	}

	/**
	 * This returns the field group that this Data Type should be listed in. See the Core::$dataTypeGroups for the
	 * available options.
	 *
	 * @return string
	 */
	final public function getDataTypeFieldGroup() {
		return $this->dataTypeFieldGroup;
	}

	/**
	 * Returns the order within the field group that this Data Type should appear.
	 *
	 * @return integer
	 */
	final public function getDataTypeFieldGroupOrder() {
		return $this->dataTypeFieldGroupOrder;
	}

	/**
	 * Returns the order in which this data type should be parsed. The generator does N number of passes for each
	 * row of data generated, each pass processes whatever data types are ...
	 *
	 * @return integer
	 */
	final public function getProcessOrder() {
		return $this->processOrder;
	}

	/**
	 * Returns the path from the generatedata root folder. That value is automatically created
	 * for each module when it it instantiated.
	 * @return string
	 */
	public final function getPath() {
		return $this->path;
	}

	/**
	 * Returns a list of all javascript modules for this Data Type (if included).
	 * @return array
	 */
	public final function getJSModules() {
		return $this->jsModules;
	}

	/**
	 * Returns the CSS filename for this Data Type (if included).
	 * @return array
	 */
	public final function getCSSFile() {
		return $this->cssFile;
	}
}