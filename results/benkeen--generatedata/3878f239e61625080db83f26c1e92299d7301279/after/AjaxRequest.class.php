<?php


/**
 * A generic class for handling all of the Core's Ajax requests. All requests are identified
 * through a unique "action" string, and (usually) arbitrary other info passed via POST.
 * @author Ben Keen <ben.keen@gmail.com>
 * @package Core
 */
class AjaxRequest {
	private $action;
	private $response;

	/**
	 * AjaxRequest objects are automatically processed when they are created, based on the unique $action
	 * value. The result of the call is stored in $response to be handled however you need (e.g. output
	 * as JSON, XML etc) - or an Exception is thrown if something went wrong. Exceptions are used SOLELY for
	 * program errors: not for user-entry errors.
	 */
	public function __construct($action, $post = array()) {
		if (empty($action)) {
			throw new Exception("no_action_specified");
			return;
		}

		$this->action = $action;
		$post = Utils::cleanHash($post);

		switch ($this->action) {

			// ------------------------------------------------------------------------------------
			// INSTALLATION
			// ------------------------------------------------------------------------------------

			// a fresh install assumes it's a blank slate: no database tables, no settings file
			case "installationTestDbSettings":
				Core::init("installation");
				list($success, $content) = Database::testDbSettings($post["dbHostname"], $post["dbName"], $post["dbUsername"], $post["dbPassword"]);
				$this->response["success"] = $success;
				$this->response["content"] = $content;
				break;

			case "installationCreateSettingsFile":
				Core::init("installation");
				if (Core::checkSettingsFileExists()) {
					$this->response["success"] = 0;
					$this->response["content"] = "Your settings.php file already exists.";
					return;
				} else {
					list($success, $content) = Installation::createSettingsFile($post["dbHostname"], $post["dbName"], $post["dbUsername"], $post["dbPassword"], $post["dbTablePrefix"]);
					$this->response["success"] = $success;
					$this->response["content"] = $content;
				}
				break;

			case "installationCreateDatabase":
				Core::init("installation_db_ready");
				list($success, $content) = Installation::createDatabase();
				if (!$success) {
					$this->response["success"] = 0;
					$this->response["content"] = $content;
					return;
				}

				// if they want user accounts, create the administrator account
				if ($post["employUserAccounts"] == "yes") {
					$adminAccount = array(
						"accountType" => "admin",
						"firstName"   => $post["firstName"],
						"lastName"    => $post["lastName"],
						"email"       => $post["email"],
						"password"    => $post["password"]
					);
					Account::createUser($adminAccount);
					Settings::setSetting("employUserAccounts", "yes");
				}

				// make note of the fact that we've passed this installation step
				Settings::setSetting("installationStepComplete_Core", "yes");

				$this->response["success"] = 1;
				$this->response["content"] = "";
				break;

			case "installationDataTypes":
				Core::init("installation_db_ready");
				$index = $post["index"];
				$groupedDataTypes = DataTypePluginHelper::getDataTypePlugins("installion_db_ready", false);
				$dataTypes = DataTypePluginHelper::getDataTypeList($groupedDataTypes);

				if ($index >= count($dataTypes)) {
					$this->response["success"] = 1;
					$this->response["content"] = "";
					$this->response["isComplete"] = true;
				} else {
					// attempt to install this data type
					$currDataType = $dataTypes[$index];
					$this->response["dataTypeName"] = $currDataType->getName();
					$this->response["dataTypeFolder"] = $currDataType->folder;
					$this->response["isComplete"] = false;

					try {
						list($success, $content) = $currDataType->install();
						$this->response["success"] = $success;
						$this->response["content"] = $content;
					} catch (Exception $e) {
						$this->response["success"] = false;
						$this->response["content"] = "Unknown error.";
					}
				}
				break;

			case "installationSaveDataTypes":
				Core::init("installation_db_ready");
				$folders = $post["folders"];
				$response = Settings::setSetting("installedDataTypes", $folders);
				$this->response["success"] = $response["success"];
				$this->response["content"] = $response["errorMessage"];
				break;

			case "installationExportTypes":
				Core::init("installation_db_ready");
				$index = $post["index"];
				$exportTypes = ExportTypePluginHelper::getExportTypePlugins("installation_db_ready", false);

				if ($index >= count($exportTypes)) {
					$this->response["success"] = 1;
					$this->response["content"] = "";
					$this->response["isComplete"] = true;
				} else {
					// attempt to install this data type
					$currExportType = $exportTypes[$index];
					$this->response["exportTypeName"] = $currExportType->getName();
					$this->response["exportTypeFolder"] = $currExportType->folder;
					$this->response["isComplete"] = false;
					try {
						list($success, $content) = $currExportType->install();
						$this->response["success"] = $success;
						$this->response["content"] = $content;
					} catch (Exception $e) {
						$this->response["success"] = false;
						$this->response["content"] = "Unknown error.";
					}
				}
				break;

			case "installationSaveExportTypes":
				Core::init("installation_db_ready");
				$folders = $post["folders"];
				$response = Settings::setSetting("installedExportTypes", $folders);
				$this->response["success"] = $response["success"];
				$this->response["content"] = $response["errorMessage"];
				break;

			case "installationCountries":
				Core::init("installation_db_ready");
				$index = $post["index"];
				$countryPlugins = CountryPluginHelper::getCountryPlugins(false);

				if ($index >= count($countryPlugins)) {
					$this->response["success"] = 1;
					$this->response["content"] = "";
					$this->response["isComplete"] = true;
				} else {
					// attempt to install this data type
					$currCountryPlugin = $countryPlugins[$index];
					$this->response["countryName"] = $currCountryPlugin->getName();
					$this->response["countryFolder"] = $currCountryPlugin->folder;
					$this->response["isComplete"] = false;
					try {
						// always run the uninstallation function first to ensure any old data is all cleared out
						$currCountryPlugin->uninstall();

						list($success, $content) = $currCountryPlugin->install();
						$this->response["success"] = $success;
						$this->response["content"] = $content;
					} catch (Exception $e) {
						$this->response["success"] = false;
						$this->response["content"] = "Unknown error.";
					}
				}
				break;

			case "installationSaveCountries":
				Core::init("installation_db_ready");
				$folders = $post["folders"];
				$response = Settings::setSetting("installedCountries", $folders);
				$response = Settings::setSetting("installationComplete", "yes");
				$this->response["success"] = $response["success"];
				$this->response["content"] = $response["errorMessage"];
				break;


			// ------------------------------------------------------------------------------------
			// USER ACCOUNTS
			// ------------------------------------------------------------------------------------

/*
			case "login":
				break;

			case "logout":
				break;

			case "loadConfiguration":
  		      $assertions = array(
					"logged_in" => true,
					"post" => array(
						"required" => "form_id",
						"numeric"  => "form_id"
					)
				);
				Utils::assert($assertions);
				$this->response = Core::$user->loadConfiguration($post["form_id"]);
				break;

			case "saveConfiguration":
//				$account_id   = $_SESSION["account_id"];
//				$form_name    = addslashes($request["form_name"]);
//				$form_content = addslashes($request["form_content"]);
				//gd_save_form($account_id, $form_name, $form_content);
				$this->response = Core::$user->saveConfiguration($post);
				break;

			case "deleteConfiguration":
				$form_id = $request["form_id"];
				gd_delete_form($form_id);
				break;

*/

			case "generateInPage":
				Core::init("generation");
				$gen = new Generator($_POST);
				$response = $gen->generate();
				$this->response["success"]    = $response["success"];
				$this->response["content"]    = $response["content"];
				$this->response["isComplete"] = $response["isComplete"];
				break;
		}
	}

	public function getResponse() {
		return $this->response;
	}
}
