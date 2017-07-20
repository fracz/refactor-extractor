<?php

final class ManiphestTaskCloseAsDuplicateRelationship
  extends ManiphestTaskRelationship {

  const RELATIONSHIPKEY = 'task.close-as-duplicate';

  public function getEdgeConstant() {
    return ManiphestTaskIsDuplicateOfTaskEdgeType::EDGECONST;
  }

  protected function getActionName() {
    return pht('Close As Duplicate');
  }

  protected function getActionIcon() {
    return 'fa-times';
  }

  public function canRelateObjects($src, $dst) {
    return ($dst instanceof ManiphestTask);
  }

  public function shouldAppearInActionMenu() {
    return false;
  }

  public function getDialogTitleText() {
    return pht('Close As Duplicate');
  }

  public function getDialogHeaderText() {
    return pht('Close This Task As a Duplicate Of');
  }

  public function getDialogButtonText() {
    return pht('Merge Into Selected Task');
  }

  protected function newRelationshipSource() {
    return new ManiphestTaskRelationshipSource();
  }

  public function getRequiredRelationshipCapabilities() {
    return array(
      PhabricatorPolicyCapability::CAN_VIEW,
      PhabricatorPolicyCapability::CAN_EDIT,
    );
  }

  public function canUndoRelationship() {
    return false;
  }

  public function willUpdateRelationships($object, array $add, array $rem) {

    // TODO: Communicate this in the UI before users hit this error.
    if (count($add) > 1) {
      throw new Exception(
        pht(
          'A task can only be closed as a duplicate of exactly one other '.
          'task.'));
    }

    $task = head($add);
    return $this->newMergeIntoTransactions($task);
  }

  public function didUpdateRelationships($object, array $add, array $rem) {
    $viewer = $this->getViewer();
    $content_source = $this->getContentSource();

    $task = head($add);
    $xactions = $this->newMergeFromTransactions(array($object));

    $task->getApplicationTransactionEditor()
      ->setActor($viewer)
      ->setContentSource($content_source)
      ->setContinueOnMissingFields(true)
      ->setContinueOnNoEffect(true)
      ->applyTransactions($task, $xactions);
  }

}