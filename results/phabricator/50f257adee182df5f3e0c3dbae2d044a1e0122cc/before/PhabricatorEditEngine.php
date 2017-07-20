<?php


/**
 * @task fields Managing Fields
 * @task text Display Text
 * @task config Edit Engine Configuration
 * @task uri Managing URIs
 * @task load Creating and Loading Objects
 * @task web Responding to Web Requests
 * @task edit Responding to Edit Requests
 * @task http Responding to HTTP Parameter Requests
 * @task conduit Responding to Conduit Requests
 */
abstract class PhabricatorEditEngine
  extends Phobject
  implements PhabricatorPolicyInterface {

  const EDITENGINECONFIG_DEFAULT = 'default';

  private $viewer;
  private $controller;
  private $isCreate;
  private $editEngineConfiguration;

  final public function setViewer(PhabricatorUser $viewer) {
    $this->viewer = $viewer;
    return $this;
  }

  final public function getViewer() {
    return $this->viewer;
  }

  final public function setController(PhabricatorController $controller) {
    $this->controller = $controller;
    $this->setViewer($controller->getViewer());
    return $this;
  }

  final public function getController() {
    return $this->controller;
  }

  final public function getEngineKey() {
    return $this->getPhobjectClassConstant('ENGINECONST', 64);
  }

  final public function getApplication() {
    $app_class = $this->getEngineApplicationClass();
    return PhabricatorApplication::getByClass($app_class);
  }


/* -(  Managing Fields  )---------------------------------------------------- */


  abstract public function getEngineApplicationClass();
  abstract protected function buildCustomEditFields($object);

  public function getFieldsForConfig(
    PhabricatorEditEngineConfiguration $config) {

    $object = $this->newEditableObject();

    $this->editEngineConfiguration = $config;

    // This is mostly making sure that we fill in default values.
    $this->setIsCreate(true);

    return $this->buildEditFields($object);
  }

  final protected function buildEditFields($object) {
    $viewer = $this->getViewer();

    $fields = $this->buildCustomEditFields($object);

    $extensions = PhabricatorEditEngineExtension::getAllEnabledExtensions();
    foreach ($extensions as $extension) {
      $extension->setViewer($viewer);

      if (!$extension->supportsObject($this, $object)) {
        continue;
      }

      $extension_fields = $extension->buildCustomEditFields($this, $object);

      // TODO: Validate this in more detail with a more tailored error.
      assert_instances_of($extension_fields, 'PhabricatorEditField');

      foreach ($extension_fields as $field) {
        $fields[] = $field;
      }
    }

    $config = $this->getEditEngineConfiguration();
    $fields = $config->applyConfigurationToFields($this, $fields);

    foreach ($fields as $field) {
      $field
        ->setViewer($viewer)
        ->setObject($object);
    }

    return $fields;
  }


/* -(  Display Text  )------------------------------------------------------- */


  /**
   * @task text
   */
  abstract public function getEngineName();


  /**
   * @task text
   */
  abstract protected function getObjectCreateTitleText($object);

  /**
   * @task text
   */
  protected function getFormHeaderText($object) {
    $config = $this->getEditEngineConfiguration();
    return $config->getName();
  }

  /**
   * @task text
   */
  abstract protected function getObjectEditTitleText($object);


  /**
   * @task text
   */
  abstract protected function getObjectCreateShortText();


  /**
   * @task text
   */
  abstract protected function getObjectEditShortText($object);


  /**
   * @task text
   */
  protected function getObjectCreateButtonText($object) {
    return $this->getObjectCreateTitleText($object);
  }


  /**
   * @task text
   */
  protected function getObjectEditButtonText($object) {
    return pht('Save Changes');
  }


/* -(  Edit Engine Configuration  )------------------------------------------ */


  protected function supportsEditEngineConfiguration() {
    return true;
  }

  final protected function getEditEngineConfiguration() {
    return $this->editEngineConfiguration;
  }

  private function loadEditEngineConfiguration($key) {
    if ($key === null) {
      $key = self::EDITENGINECONFIG_DEFAULT;
    }

    $config = id(new PhabricatorEditEngineConfigurationQuery())
      ->setViewer($this->getViewer())
      ->withEngineKeys(array($this->getEngineKey()))
      ->withIdentifiers(array($key))
      ->executeOne();
    if (!$config) {
      return null;
    }

    $this->editEngineConfiguration = $config;

    return $config;
  }

  final public function getBuiltinEngineConfigurations() {
    $configurations = $this->newBuiltinEngineConfigurations();

    if (!$configurations) {
      throw new Exception(
        pht(
          'EditEngine ("%s") returned no builtin engine configurations, but '.
          'an edit engine must have at least one configuration.',
          get_class($this)));
    }

    assert_instances_of($configurations, 'PhabricatorEditEngineConfiguration');

    $has_default = false;
    foreach ($configurations as $config) {
      if ($config->getBuiltinKey() == self::EDITENGINECONFIG_DEFAULT) {
        $has_default = true;
      }
    }

    if (!$has_default) {
      $first = head($configurations);
      if (!$first->getBuiltinKey()) {
        $first
          ->setBuiltinKey(self::EDITENGINECONFIG_DEFAULT)
          ->setIsDefault(true);

        if (!strlen($first->getName())) {
          $first->setName($this->getObjectCreateShortText());
        }
    } else {
        throw new Exception(
          pht(
            'EditEngine ("%s") returned builtin engine configurations, '.
            'but none are marked as default and the first configuration has '.
            'a different builtin key already. Mark a builtin as default or '.
            'omit the key from the first configuration',
            get_class($this)));
      }
    }

    $builtins = array();
    foreach ($configurations as $key => $config) {
      $builtin_key = $config->getBuiltinKey();

      if ($builtin_key === null) {
        throw new Exception(
          pht(
            'EditEngine ("%s") returned builtin engine configurations, '.
            'but one (with key "%s") is missing a builtin key. Provide a '.
            'builtin key for each configuration (you can omit it from the '.
            'first configuration in the list to automatically assign the '.
            'default key).',
            get_class($this),
            $key));
      }

      if (isset($builtins[$builtin_key])) {
        throw new Exception(
          pht(
            'EditEngine ("%s") returned builtin engine configurations, '.
            'but at least two specify the same builtin key ("%s"). Engines '.
            'must have unique builtin keys.',
            get_class($this),
            $builtin_key));
      }

      $builtins[$builtin_key] = $config;
    }


    return $builtins;
  }

  protected function newBuiltinEngineConfigurations() {
    return array(
      $this->newConfiguration(),
    );
  }

  final protected function newConfiguration() {
    return PhabricatorEditEngineConfiguration::initializeNewConfiguration(
      $this->getViewer(),
      $this);
  }


/* -(  Managing URIs  )------------------------------------------------------ */


  /**
   * @task uri
   */
  abstract protected function getObjectViewURI($object);


  /**
   * @task uri
   */
  protected function getObjectCreateCancelURI($object) {
    return $this->getApplication()->getApplicationURI();
  }


  /**
   * @task uri
   */
  protected function getEditorURI() {
    return $this->getApplication()->getApplicationURI('edit/');
  }


  /**
   * @task uri
   */
  protected function getObjectEditCancelURI($object) {
    return $this->getObjectViewURI($object);
  }


  /**
   * @task uri
   */
  public function getEditURI($object = null, $path = null) {
    $parts = array();

    $parts[] = $this->getEditorURI();

    if ($object && $object->getID()) {
      $parts[] = $object->getID().'/';
    }

    if ($path !== null) {
      $parts[] = $path;
    }

    return implode('', $parts);
  }


/* -(  Creating and Loading Objects  )--------------------------------------- */


  /**
   * Initialize a new object for creation.
   *
   * @return object Newly initialized object.
   * @task load
   */
  abstract protected function newEditableObject();


  /**
   * Build an empty query for objects.
   *
   * @return PhabricatorPolicyAwareQuery Query.
   * @task load
   */
  abstract protected function newObjectQuery();


  /**
   * Test if this workflow is creating a new object or editing an existing one.
   *
   * @return bool True if a new object is being created.
   * @task load
   */
  final public function getIsCreate() {
    return $this->isCreate;
  }


  /**
   * Flag this workflow as a create or edit.
   *
   * @param bool True if this is a create workflow.
   * @return this
   * @task load
   */
  private function setIsCreate($is_create) {
    $this->isCreate = $is_create;
    return $this;
  }


  /**
   * Load an object by ID.
   *
   * @param int Object ID.
   * @return object|null Object, or null if no such object exists.
   * @task load
   */
  private function newObjectFromID($id) {
    $query = $this->newObjectQuery()
      ->withIDs(array($id));

    return $this->newObjectFromQuery($query);
  }


  /**
   * Load an object by PHID.
   *
   * @param phid Object PHID.
   * @return object|null Object, or null if no such object exists.
   * @task load
   */
  private function newObjectFromPHID($phid) {
    $query = $this->newObjectQuery()
      ->withPHIDs(array($phid));

    return $this->newObjectFromQuery($query);
  }


  /**
   * Load an object given a configured query.
   *
   * @param PhabricatorPolicyAwareQuery Configured query.
   * @return object|null Object, or null if no such object exists.
   * @task load
   */
  private function newObjectFromQuery(PhabricatorPolicyAwareQuery $query) {
    $viewer = $this->getViewer();

    $object = $query
      ->setViewer($viewer)
      ->requireCapabilities(
        array(
          PhabricatorPolicyCapability::CAN_VIEW,
          PhabricatorPolicyCapability::CAN_EDIT,
        ))
      ->executeOne();
    if (!$object) {
      return null;
    }

    return $object;
  }


  /**
   * Verify that an object is appropriate for editing.
   *
   * @param wild Loaded value.
   * @return void
   * @task load
   */
  private function validateObject($object) {
    if (!$object || !is_object($object)) {
      throw new Exception(
        pht(
          'EditEngine "%s" created or loaded an invalid object: object must '.
          'actually be an object, but is of some other type ("%s").',
          get_class($this),
          gettype($object)));
    }

    if (!($object instanceof PhabricatorApplicationTransactionInterface)) {
      throw new Exception(
        pht(
          'EditEngine "%s" created or loaded an invalid object: object (of '.
          'class "%s") must implement "%s", but does not.',
          get_class($this),
          get_class($object),
          'PhabricatorApplicationTransactionInterface'));
    }
  }


/* -(  Responding to Web Requests  )----------------------------------------- */


  final public function buildResponse() {
    $viewer = $this->getViewer();
    $controller = $this->getController();
    $request = $controller->getRequest();

    $form_key = $request->getURIData('formKey');
    $config = $this->loadEditEngineConfiguration($form_key);
    if (!$config) {
      return new Aphront404Response();
    }

    $id = $request->getURIData('id');
    if ($id) {
      $this->setIsCreate(false);
      $object = $this->newObjectFromID($id);
      if (!$object) {
        return new Aphront404Response();
      }
    } else {
      $this->setIsCreate(true);
      $object = $this->newEditableObject();
    }

    $this->validateObject($object);

    $action = $request->getURIData('editAction');
    switch ($action) {
      case 'parameters':
        return $this->buildParametersResponse($object);
      case 'nodefault':
        return $this->buildNoDefaultResponse($object);
      default:
        return $this->buildEditResponse($object);
    }
  }

  private function buildCrumbs($object, $final = false) {
    $controller = $this->getcontroller();

    $crumbs = $controller->buildApplicationCrumbsForEditEngine();
    if ($this->getIsCreate()) {
      $create_text = $this->getObjectCreateShortText();
      if ($final) {
        $crumbs->addTextCrumb($create_text);
      } else {
        $edit_uri = $this->getEditURI($object);
        $crumbs->addTextCrumb($create_text, $edit_uri);
      }
    } else {
      $crumbs->addTextCrumb(
        $this->getObjectEditShortText($object),
        $this->getObjectViewURI($object));

      $edit_text = pht('Edit');
      if ($final) {
        $crumbs->addTextCrumb($edit_text);
      } else {
        $edit_uri = $this->getEditURI($object);
        $crumbs->addTextCrumb($edit_text, $edit_uri);
      }
    }

    return $crumbs;
  }

  private function buildEditResponse($object) {
    $viewer = $this->getViewer();
    $controller = $this->getController();
    $request = $controller->getRequest();

    $fields = $this->buildEditFields($object);
    $template = $object->getApplicationTransactionTemplate();

    $validation_exception = null;
    if ($request->isFormPost()) {
      foreach ($fields as $field) {
        $field->setIsSubmittedForm(true);

        if ($field->getIsLocked() || $field->getIsHidden()) {
          continue;
        }

        $field->readValueFromSubmit($request);
      }

      $xactions = array();
      foreach ($fields as $field) {
        $xaction = $field->generateTransaction(clone $template);

        if (!$xaction) {
          continue;
        }

        $xactions[] = $xaction;
      }

      $editor = $object->getApplicationTransactionEditor()
        ->setActor($viewer)
        ->setContentSourceFromRequest($request)
        ->setContinueOnNoEffect(true);

      try {

        $editor->applyTransactions($object, $xactions);

        return id(new AphrontRedirectResponse())
          ->setURI($this->getObjectViewURI($object));
      } catch (PhabricatorApplicationTransactionValidationException $ex) {
        $validation_exception = $ex;

        foreach ($fields as $field) {
          $xaction_type = $field->getTransactionType();
          if ($xaction_type === null) {
            continue;
          }

          $message = $ex->getShortMessage($xaction_type);
          if ($message === null) {
            continue;
          }

          $field->setControlError($message);
        }
      }
    } else {
      if ($this->getIsCreate()) {
        foreach ($fields as $field) {
          if ($field->getIsLocked() || $field->getIsHidden()) {
            continue;
          }

          $field->readValueFromRequest($request);
        }
      } else {
        foreach ($fields as $field) {
          $field->readValueFromObject($object);
        }
      }
    }

    $action_button = $this->buildEditFormActionButton($object);

    if ($this->getIsCreate()) {
      $header_text = $this->getFormHeaderText($object);
    } else {
      $header_text = $this->getObjectEditTitleText($object);
    }

    $header = id(new PHUIHeaderView())
      ->setHeader($header_text)
      ->addActionLink($action_button);

    $crumbs = $this->buildCrumbs($object, $final = true);
    $form = $this->buildEditForm($object, $fields);

    $box = id(new PHUIObjectBoxView())
      ->setUser($viewer)
      ->setHeader($header)
      ->setValidationException($validation_exception)
      ->appendChild($form);

    return $controller->newPage()
      ->setTitle($header_text)
      ->setCrumbs($crumbs)
      ->appendChild($box);
  }

  private function buildEditForm($object, array $fields) {
    $viewer = $this->getViewer();

    $form = id(new AphrontFormView())
      ->setUser($viewer);

    foreach ($fields as $field) {
      $field->appendToForm($form);
    }

    if ($this->getIsCreate()) {
      $cancel_uri = $this->getObjectCreateCancelURI($object);
      $submit_button = $this->getObjectCreateButtonText($object);
    } else {
      $cancel_uri = $this->getObjectEditCancelURI($object);
      $submit_button = $this->getObjectEditButtonText($object);
    }

    $form->appendControl(
      id(new AphrontFormSubmitControl())
        ->addCancelButton($cancel_uri)
        ->setValue($submit_button));

    return $form;
  }

  private function buildEditFormActionButton($object) {
    $viewer = $this->getViewer();

    $action_view = id(new PhabricatorActionListView())
      ->setUser($viewer);

    foreach ($this->buildEditFormActions($object) as $action) {
      $action_view->addAction($action);
    }

    $action_button = id(new PHUIButtonView())
      ->setTag('a')
      ->setText(pht('Actions'))
      ->setHref('#')
      ->setIconFont('fa-bars')
      ->setDropdownMenu($action_view);

    return $action_button;
  }

  private function buildEditFormActions($object) {
    $actions = array();

    if ($this->supportsEditEngineConfiguration()) {
      $engine_key = $this->getEngineKey();
      $config = $this->getEditEngineConfiguration();

      $actions[] = id(new PhabricatorActionView())
        ->setName(pht('Manage Form Configurations'))
        ->setIcon('fa-list-ul')
        ->setHref("/transactions/editengine/{$engine_key}/");
      $actions[] = id(new PhabricatorActionView())
        ->setName(pht('Edit Form Configuration'))
        ->setIcon('fa-pencil')
        ->setHref($config->getURI());
    }

    $actions[] = id(new PhabricatorActionView())
      ->setName(pht('Show HTTP Parameters'))
      ->setIcon('fa-crosshairs')
      ->setHref($this->getEditURI($object, 'parameters/'));

    return $actions;
  }

  final public function addActionToCrumbs(PHUICrumbsView $crumbs) {
    $viewer = $this->getViewer();

    $configs = id(new PhabricatorEditEngineConfigurationQuery())
      ->setViewer($viewer)
      ->withEngineKeys(array($this->getEngineKey()))
      ->withIsDefault(true)
      ->withIsDisabled(false)
      ->execute();

    $dropdown = null;
    $disabled = false;
    $workflow = false;

    $menu_icon = 'fa-plus-square';

    if (!$configs) {
      if ($viewer->isLoggedIn()) {
        $disabled = true;
      } else {
        // If the viewer isn't logged in, assume they'll get hit with a login
        // dialog and are likely able to create objects after they log in.
        $disabled = false;
      }
      $workflow = true;
      $create_uri = $this->getEditURI(null, 'nodefault/');
    } else {
      $config = head($configs);
      $form_key = $config->getIdentifier();
      $create_uri = $this->getEditURI(null, "form/{$form_key}/");

      if (count($configs) > 1) {
        $configs = msort($configs, 'getDisplayName');

        $menu_icon = 'fa-caret-square-o-down';

        $dropdown = id(new PhabricatorActionListView())
          ->setUser($viewer);

        foreach ($configs as $config) {
          $form_key = $config->getIdentifier();
          $config_uri = $this->getEditURI(null, "form/{$form_key}/");

          $item_icon = 'fa-plus';

          $dropdown->addAction(
            id(new PhabricatorActionView())
              ->setName($config->getDisplayName())
              ->setIcon($item_icon)
              ->setHref($config_uri));
        }
      }
    }

    $action = id(new PHUIListItemView())
      ->setName($this->getObjectCreateShortText())
      ->setHref($create_uri)
      ->setIcon($menu_icon)
      ->setWorkflow($workflow)
      ->setDisabled($disabled);

    if ($dropdown) {
      $action->setDropdownMenu($dropdown);
    }

    $crumbs->addAction($action);
  }


/* -(  Responding to HTTP Parameter Requests  )------------------------------ */


  /**
   * Respond to a request for documentation on HTTP parameters.
   *
   * @param object Editable object.
   * @return AphrontResponse Response object.
   * @task http
   */
  private function buildParametersResponse($object) {
    $controller = $this->getController();
    $viewer = $this->getViewer();
    $request = $controller->getRequest();
    $fields = $this->buildEditFields($object);

    $crumbs = $this->buildCrumbs($object);
    $crumbs->addTextCrumb(pht('HTTP Parameters'));
    $crumbs->setBorder(true);

    $header_text = pht(
      'HTTP Parameters: %s',
      $this->getObjectCreateShortText());

    $header = id(new PHUIHeaderView())
      ->setHeader($header_text);

    $help_view = id(new PhabricatorApplicationEditHTTPParameterHelpView())
      ->setUser($viewer)
      ->setFields($fields);

    $document = id(new PHUIDocumentViewPro())
      ->setUser($viewer)
      ->setHeader($header)
      ->appendChild($help_view);

    return $controller->newPage()
      ->setTitle(pht('HTTP Parameters'))
      ->setCrumbs($crumbs)
      ->appendChild($document);
  }


  private function buildNoDefaultResponse($object) {
    $cancel_uri = $this->getObjectCreateCancelURI($object);

    return $this->getController()
      ->newDialog()
      ->setTitle(pht('No Default Create Forms'))
      ->appendParagraph(
        pht(
          'This application is not configured with any visible, enabled '.
          'forms for creating objects.'))
      ->addCancelButton($cancel_uri);
  }

/* -(  Conduit  )------------------------------------------------------------ */


  /**
   * Respond to a Conduit edit request.
   *
   * This method accepts a list of transactions to apply to an object, and
   * either edits an existing object or creates a new one.
   *
   * @task conduit
   */
  final public function buildConduitResponse(ConduitAPIRequest $request) {
    $viewer = $this->getViewer();

    $config = $this->loadEditEngineConfiguration(null);
    if (!$config) {
      throw new Exception(
        pht(
          'Unable to load configuration for this EditEngine ("%s").',
          get_class($this)));
    }

    $phid = $request->getValue('objectPHID');
    if ($phid) {
      $this->setIsCreate(false);
      $object = $this->newObjectFromPHID($phid);
      if (!$object) {
        throw new Exception(pht('No such object with PHID "%s".', $phid));
      }
    } else {
      $this->setIsCreate(true);
      $object = $this->newEditableObject();
    }

    $this->validateObject($object);

    $fields = $this->buildEditFields($object);

    $types = $this->getAllEditTypesFromFields($fields);
    $template = $object->getApplicationTransactionTemplate();

    $xactions = $this->getConduitTransactions($request, $types, $template);

    $editor = $object->getApplicationTransactionEditor()
      ->setActor($viewer)
      ->setContentSourceFromConduitRequest($request)
      ->setContinueOnNoEffect(true);

    $xactions = $editor->applyTransactions($object, $xactions);

    $xactions_struct = array();
    foreach ($xactions as $xaction) {
      $xactions_struct[] = array(
        'phid' => $xaction->getPHID(),
      );
    }

    return array(
      'object' => array(
        'id' => $object->getID(),
        'phid' => $object->getPHID(),
      ),
      'transactions' => $xactions_struct,
    );
  }


  /**
   * Generate transactions which can be applied from edit actions in a Conduit
   * request.
   *
   * @param ConduitAPIRequest The request.
   * @param list<PhabricatorEditType> Supported edit types.
   * @param PhabricatorApplicationTransaction Template transaction.
   * @return list<PhabricatorApplicationTransaction> Generated transactions.
   * @task conduit
   */
  private function getConduitTransactions(
    ConduitAPIRequest $request,
    array $types,
    PhabricatorApplicationTransaction $template) {

    $transactions_key = 'transactions';

    $xactions = $request->getValue($transactions_key);
    if (!is_array($xactions)) {
      throw new Exception(
        pht(
          'Parameter "%s" is not a list of transactions.',
          $transactions_key));
    }

    foreach ($xactions as $key => $xaction) {
      if (!is_array($xaction)) {
        throw new Exception(
          pht(
            'Parameter "%s" must contain a list of transaction descriptions, '.
            'but item with key "%s" is not a dictionary.',
            $transactions_key,
            $key));
      }

      if (!array_key_exists('type', $xaction)) {
        throw new Exception(
          pht(
            'Parameter "%s" must contain a list of transaction descriptions, '.
            'but item with key "%s" is missing a "type" field. Each '.
            'transaction must have a type field.',
            $transactions_key,
            $key));
      }

      $type = $xaction['type'];
      if (empty($types[$type])) {
        throw new Exception(
          pht(
            'Transaction with key "%s" has invalid type "%s". This type is '.
            'not recognized. Valid types are: %s.',
            $key,
            $type,
            implode(', ', array_keys($types))));
      }
    }

    $results = array();
    foreach ($xactions as $xaction) {
      $type = $types[$xaction['type']];

      $results[] = $type->generateTransaction(
        clone $template,
        $xaction);
    }

    return $results;
  }


  /**
   * @return map<string, PhabricatorEditType>
   * @task conduit
   */
  private function getAllEditTypesFromFields(array $fields) {
    $types = array();
    foreach ($fields as $field) {
      $field_types = $field->getEditTransactionTypes();
      foreach ($field_types as $field_type) {
        $field_type->setField($field);
        $types[$field_type->getEditType()] = $field_type;
      }
    }
    return $types;
  }

  public function getAllEditTypes() {
    $config = $this->loadEditEngineConfiguration(null);
    if (!$config) {
      return array();
    }

    $object = $this->newEditableObject();
    $fields = $this->buildEditFields($object);
    return $this->getAllEditTypesFromFields($fields);
  }

  final public static function getAllEditEngines() {
    return id(new PhutilClassMapQuery())
      ->setAncestorClass(__CLASS__)
      ->setUniqueMethod('getEngineKey')
      ->execute();
  }

  final public static function getByKey(PhabricatorUser $viewer, $key) {
    return id(new PhabricatorEditEngineQuery())
      ->setViewer($viewer)
      ->withEngineKeys(array($key))
      ->executeOne();
  }


/* -(  PhabricatorPolicyInterface  )----------------------------------------- */


  public function getPHID() {
    return get_class($this);
  }

  public function getCapabilities() {
    return array(
      PhabricatorPolicyCapability::CAN_VIEW,
    );
  }

  public function getPolicy($capability) {
    switch ($capability) {
      case PhabricatorPolicyCapability::CAN_VIEW:
        return PhabricatorPolicies::getMostOpenPolicy();
    }
  }

  public function hasAutomaticCapability($capability, PhabricatorUser $viewer) {
    return false;
  }

  public function describeAutomaticCapability($capability) {
    return null;
  }
}