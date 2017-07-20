<?php

final class PhabricatorSimpleEditType extends PhabricatorEditType {

  private $valueType;
  private $valueDescription;
  private $phuixControlType;
  private $phuixControlSpecification = array();

  public function setValueType($value_type) {
    $this->valueType = $value_type;
    return $this;
  }

  public function getValueType() {
    return $this->valueType;
  }

  public function generateTransactions(
    PhabricatorApplicationTransaction $template,
    array $spec) {

    $edit = $this->newTransaction($template)
      ->setNewValue(idx($spec, 'value'));

    return array($edit);
  }

  public function setValueDescription($value_description) {
    $this->valueDescription = $value_description;
    return $this;
  }

  public function getValueDescription() {
    return $this->valueDescription;
  }

}