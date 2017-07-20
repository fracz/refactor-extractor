<?php

final class DrydockResource extends DrydockDAO
  implements PhabricatorPolicyInterface {

  protected $id;
  protected $phid;
  protected $blueprintPHID;
  protected $status;
  protected $until;
  protected $type;
  protected $name;
  protected $attributes   = array();
  protected $capabilities = array();
  protected $ownerPHID;

  private $blueprint = self::ATTACHABLE;
  private $unconsumedCommands = self::ATTACHABLE;

  private $isAllocated = false;
  private $isActivated = false;
  private $activateWhenAllocated = false;
  private $slotLocks = array();

  protected function getConfiguration() {
    return array(
      self::CONFIG_AUX_PHID => true,
      self::CONFIG_SERIALIZATION => array(
        'attributes'    => self::SERIALIZATION_JSON,
        'capabilities'  => self::SERIALIZATION_JSON,
      ),
      self::CONFIG_COLUMN_SCHEMA => array(
        'name' => 'text255',
        'ownerPHID' => 'phid?',
        'status' => 'text32',
        'type' => 'text64',
        'until' => 'epoch?',
      ),
      self::CONFIG_KEY_SCHEMA => array(
        'key_type' => array(
          'columns' => array('type', 'status'),
        ),
        'key_blueprint' => array(
          'columns' => array('blueprintPHID', 'status'),
        ),
      ),
    ) + parent::getConfiguration();
  }

  public function generatePHID() {
    return PhabricatorPHID::generateNewPHID(DrydockResourcePHIDType::TYPECONST);
  }

  public function getAttribute($key, $default = null) {
    return idx($this->attributes, $key, $default);
  }

  public function getAttributesForTypeSpec(array $attribute_names) {
    return array_select_keys($this->attributes, $attribute_names);
  }

  public function setAttribute($key, $value) {
    $this->attributes[$key] = $value;
    return $this;
  }

  public function getCapability($key, $default = null) {
    return idx($this->capbilities, $key, $default);
  }

  public function getInterface(DrydockLease $lease, $type) {
    return $this->getBlueprint()->getInterface($this, $lease, $type);
  }

  public function getBlueprint() {
    return $this->assertAttached($this->blueprint);
  }

  public function attachBlueprint(DrydockBlueprint $blueprint) {
    $this->blueprint = $blueprint;
    return $this;
  }

  public function getUnconsumedCommands() {
    return $this->assertAttached($this->unconsumedCommands);
  }

  public function attachUnconsumedCommands(array $commands) {
    $this->unconsumedCommands = $commands;
    return $this;
  }

  public function isReleasing() {
    foreach ($this->getUnconsumedCommands() as $command) {
      if ($command->getCommand() == DrydockCommand::COMMAND_RELEASE) {
        return true;
      }
    }

    return false;
  }

  public function setActivateWhenAllocated($activate) {
    $this->activateWhenAllocated = $activate;
    return $this;
  }

  public function needSlotLock($key) {
    $this->slotLocks[] = $key;
    return $this;
  }

  public function allocateResource() {
    if ($this->getID()) {
      throw new Exception(
        pht(
          'Trying to allocate a resource which has already been persisted. '.
          'Only new resources may be allocated.'));
    }

    $expect_status = DrydockResourceStatus::STATUS_PENDING;
    $actual_status = $this->getStatus();
    if ($actual_status != $expect_status) {
      throw new Exception(
        pht(
          'Trying to allocate a resource from the wrong status. Status must '.
          'be "%s", actually "%s".',
          $expect_status,
          $actual_status));
    }

    if ($this->activateWhenAllocated) {
      $new_status = DrydockResourceStatus::STATUS_ACTIVE;
    } else {
      $new_status = DrydockResourceStatus::STATUS_PENDING;
    }

    $phid = $this->generatePHID();

    $this->openTransaction();
    try {
      try {
        DrydockSlotLock::acquireLocks($phid, $this->slotLocks);
        $this->slotLocks = array();
      } catch (DrydockSlotLockException $ex) {
        $this->logEvent(
          DrydockSlotLockFailureLogType::LOGCONST,
          array(
            'locks' => $ex->getLockMap(),
          ));
        throw $ex;
      }

      $this
        ->setPHID($phid)
        ->setStatus($new_status)
        ->save();
    } catch (Exception $ex) {
      $this->killTransaction();
      throw $ex;
    }
    $this->saveTransaction();

    $this->isAllocated = true;

    if ($new_status == DrydockResourceStatus::STATUS_ACTIVE) {
      $this->didActivate();
    }

    return $this;
  }

  public function isAllocatedResource() {
    return $this->isAllocated;
  }

  public function activateResource() {
    if (!$this->getID()) {
      throw new Exception(
        pht(
          'Trying to activate a resource which has not yet been persisted.'));
    }

    $expect_status = DrydockResourceStatus::STATUS_PENDING;
    $actual_status = $this->getStatus();
    if ($actual_status != $expect_status) {
      throw new Exception(
        pht(
          'Trying to activate a resource from the wrong status. Status must '.
          'be "%s", actually "%s".',
          $expect_status,
          $actual_status));
    }

    $this->openTransaction();

      $this
        ->setStatus(DrydockResourceStatus::STATUS_ACTIVE)
        ->save();

      DrydockSlotLock::acquireLocks($this->getPHID(), $this->slotLocks);
      $this->slotLocks = array();

    $this->saveTransaction();

    $this->isActivated = true;

    $this->didActivate();

    return $this;
  }

  public function isActivatedResource() {
    return $this->isActivated;
  }

  public function canRelease() {
    switch ($this->getStatus()) {
      case DrydockResourceStatus::STATUS_RELEASED:
      case DrydockResourceStatus::STATUS_DESTROYED:
        return false;
      default:
        return true;
    }
  }

  public function scheduleUpdate($epoch = null) {
    PhabricatorWorker::scheduleTask(
      'DrydockResourceUpdateWorker',
      array(
        'resourcePHID' => $this->getPHID(),
        'isExpireTask' => ($epoch !== null),
      ),
      array(
        'objectPHID' => $this->getPHID(),
        'delayUntil' => ($epoch ? (int)$epoch : null),
      ));
  }

  private function didActivate() {
    $viewer = PhabricatorUser::getOmnipotentUser();

    $need_update = false;

    $commands = id(new DrydockCommandQuery())
      ->setViewer($viewer)
      ->withTargetPHIDs(array($this->getPHID()))
      ->withConsumed(false)
      ->execute();
    if ($commands) {
      $need_update = true;
    }

    if ($need_update) {
      $this->scheduleUpdate();
    }

    $expires = $this->getUntil();
    if ($expires) {
      $this->scheduleUpdate($expires);
    }
  }

  public function canReceiveCommands() {
    switch ($this->getStatus()) {
      case DrydockResourceStatus::STATUS_RELEASED:
      case DrydockResourceStatus::STATUS_BROKEN:
      case DrydockResourceStatus::STATUS_DESTROYED:
        return false;
      default:
        return true;
    }
  }

  public function logEvent($type, array $data = array()) {
    $log = id(new DrydockLog())
      ->setEpoch(PhabricatorTime::getNow())
      ->setType($type)
      ->setData($data);

    $log->setResourcePHID($this->getPHID());
    $log->setBlueprintPHID($this->getBlueprintPHID());

    return $log->save();
  }


/* -(  PhabricatorPolicyInterface  )----------------------------------------- */


  public function getCapabilities() {
    return array(
      PhabricatorPolicyCapability::CAN_VIEW,
      PhabricatorPolicyCapability::CAN_EDIT,
    );
  }

  public function getPolicy($capability) {
    return $this->getBlueprint()->getPolicy($capability);
  }

  public function hasAutomaticCapability($capability, PhabricatorUser $viewer) {
    return $this->getBlueprint()->hasAutomaticCapability(
      $capability,
      $viewer);
  }

  public function describeAutomaticCapability($capability) {
    return pht('Resources inherit the policies of their blueprints.');
  }
}