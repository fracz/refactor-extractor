<?php

/*
 * Copyright 2011 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class PhabricatorConduitAPIController
  extends PhabricatorConduitController {

  public function shouldRequireLogin() {
    return false;
  }

  private $method;

  public function willProcessRequest(array $data) {
    $this->method = $data['method'];
    return $this;
  }

  public function processRequest() {
    $time_start = microtime(true);
    $request = $this->getRequest();

    $method = $this->method;

    $method_class = ConduitAPIMethod::getClassNameFromAPIMethodName($method);
    $api_request = null;

    $log = new PhabricatorConduitMethodCallLog();
    $log->setMethod($method);
    $metadata = array();

    try {

      if (!class_exists($method_class)) {
        throw new Exception(
          "Unable to load the implementation class for method '{$method}'. ".
          "You may have misspelled the method, need to define ".
          "'{$method_class}', or need to run 'arc build'.");
      }

      // Fake out checkModule, the class has already been autoloaded by the
      // class_exists() call above.
      $method_handler = newv($method_class, array());

      if (isset($_REQUEST['params']) && is_array($_REQUEST['params'])) {
        $params_post = $request->getArr('params');
        foreach ($params_post as $key => $value) {
          $params_post[$key] = json_decode($value, true);
        }
        $params = $params_post;
      } else {
        $params_json = $request->getStr('params');
        if (!strlen($params_json)) {
          $params = array();
        } else {
          $params = json_decode($params_json, true);
          if (!is_array($params)) {
            throw new Exception(
              "Invalid parameter information was passed to method ".
              "'{$method}', could not decode JSON serialization.");
          }
        }
      }

      $metadata = idx($params, '__conduit__', array());
      unset($params['__conduit__']);

      $api_request = new ConduitAPIRequest($params);

      if ($request->getUser()->getPHID()) {
        $auth_okay = true;
      } else if (!$method_handler->shouldRequireAuthentication()) {
        $auth_okay = true;
      } else {
        $session_key = idx($metadata, 'sessionKey');
        if (!$session_key) {
          $auth_okay = false;
          $error_code = 'ERR-NO-CERTIFICATE';
          $error_info = "This server requires authentication but your client ".
                        "is not configured with an authentication ".
                        "certificate. Please refer to ".
                        "page http://www.phabricator.com/docs/".
                        "phabricator/article/".
                        "Installing_Arcanist_Certificates.html for more info.";
        } else {
          $user = new PhabricatorUser();
          $session = queryfx_one(
            $user->establishConnection('r'),
            'SELECT * FROM %T WHERE sessionKey = %s',
            PhabricatorUser::SESSION_TABLE,
            $session_key);
          if (!$session) {
            $auth_okay = false;
            $result = null;
            $error_code = 'ERR-INVALID-SESSION';
            $error_info = 'Session key is invalid.';
          } else {
            // TODO: Make sessions timeout.
            $auth_okay = true;
          }
        }
        // TODO: When we session, read connectionID from the session table.
      }

      if ($auth_okay) {
        try {
          $result = $method_handler->executeMethod($api_request);
          $error_code = null;
          $error_info = null;
        } catch (ConduitException $ex) {
          $result = null;
          $error_code = $ex->getMessage();
          $error_info = $method_handler->getErrorDescription($error_code);
        }
      }
    } catch (Exception $ex) {
      $result = null;
      $error_code = 'ERR-CONDUIT-CORE';
      $error_info = $ex->getMessage();
    }

    $time_end = microtime(true);

    $connection_id = null;
    if (idx($metadata, 'connectionID')) {
      $connection_id = $metadata['connectionID'];
    } else if (($method == 'conduit.connect') && $result) {
      $connection_id = idx($result, 'connectionID');
    }

    $log->setConnectionID($connection_id);
    $log->setError((string)$error_code);
    $log->setDuration(1000000 * ($time_end - $time_start));
    $log->save();

    $result = array(
      'result'      => $result,
      'error_code'  => $error_code,
      'error_info'  => $error_info,
    );

    switch ($request->getStr('output')) {
      case 'human':
        return $this->buildHumanReadableResponse(
          $method,
          $api_request,
          $result);
      case 'json':
      default:
        return id(new AphrontFileResponse())
          ->setMimeType('application/json')
          ->setContent('for(;;);'.json_encode($result));
    }
  }

  private function buildHumanReadableResponse(
    $method,
    ConduitAPIRequest $request = null,
    $result = null) {

    $param_rows = array();
    $param_rows[] = array('Method', phutil_escape_html($method));
    if ($request) {
      foreach ($request->getAllParameters() as $key => $value) {
        $param_rows[] = array(
          phutil_escape_html($key),
          phutil_escape_html(json_encode($value)),
        );
      }
    }

    $param_table = new AphrontTableView($param_rows);
    $param_table->setColumnClasses(
      array(
        'header',
        'wide',
      ));

    $result_rows = array();
    foreach ($result as $key => $value) {
      $result_rows[] = array(
        phutil_escape_html($key),
        phutil_escape_html(json_encode($value)),
      );
    }

    $result_table = new AphrontTableView($result_rows);
    $result_table->setColumnClasses(
      array(
        'header',
        'wide',
      ));

    $param_panel = new AphrontPanelView();
    $param_panel->setHeader('Method Parameters');
    $param_panel->appendChild($param_table);

    $result_panel = new AphrontPanelView();
    $result_panel->setHeader('Method Result');
    $result_panel->appendChild($result_table);

    return $this->buildStandardPageResponse(
      array(
        $param_panel,
        $result_panel,
      ),
      array(
        'title' => 'Method Call Result',
      ));
  }

}