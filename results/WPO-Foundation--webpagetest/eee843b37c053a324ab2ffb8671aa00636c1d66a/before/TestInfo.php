<?php

require_once __DIR__ . '/../common_lib.inc'; // TODO: remove if we don't use GetTestInfo anymore

class TestInfo {

  private $id;
  private $rootDirectory;
  private $rawData;


  private function __construct($id, $rootDirectory, &$testInfo) {
    // This isn't likely to stay the standard constructor, so we name it explicitly as a static function below
    $this->id = $id;
    $this->rootDirectory = $rootDirectory;
    $this->rawData = &$testInfo;
  }

  /**
   * @param string $id The test id
   * @param string $rootDirectory The root directory of the test data
   * @param array $testInfo Array with information about the test
   * @return TestInfo The created instance
   */
  public static function fromValues($id, $rootDirectory, &$testInfo) {
    return new self($id, $rootDirectory, $testInfo);
  }

  public static function fromFiles($rootDirectory, $touchFile = true) {
    $test = array();
    $iniPath = $rootDirectory . "/testinfo.ini";
    if (is_file($iniPath)) {
      $test = parse_ini_file($iniPath, true);
      if (!$touchFile)
        touch($iniPath);
    }
    $test["testinfo"] = GetTestInfo($rootDirectory);
    return new self($test['testinfo']["id"], $rootDirectory, $test);
  }

  /**
   * @return string The id of the test
   */
  public function getId() {
    return $this->id;
  }

  /**
   * @return string The root directory for the test, relative to the WebpageTest root
   */
  public function getRootDirectory() {
    return $this->rootDirectory;
  }

  /**
   * @return string|null The location as saved in the ini file or null if not set
   */
  public function getTestLocation() {
    if (empty($this->rawData['test']['location'])) {
      return null;
    }
    return $this->rawData['test']['location'];
  }

  /**
   * @return array The test info as saved in testinfo.json
   */
  public function getInfoArray() {
    return empty($this->rawData["testinfo"]) ? null : $this->rawData["testinfo"];
  }

  /**
   * @param int $run The run number
   * @return null|string Tester for specified run
   */
  public function getTester($run) {
    if (!array_key_exists('testinfo', $this->rawData)) {
      return null;
    }
    $tester = null;
    if (array_key_exists('tester', $this->rawData['testinfo']))
      $tester = $this->rawData['testinfo']['tester'];
    if (array_key_exists('test_runs', $this->rawData['testinfo']) &&
      array_key_exists($run, $this->rawData['testinfo']['test_runs']) &&
      array_key_exists('tester', $this->rawData['testinfo']['test_runs'][$run])
    )
      $tester = $this->rawData['testinfo']['test_runs'][$run]['tester'];
    return $tester;
  }
}