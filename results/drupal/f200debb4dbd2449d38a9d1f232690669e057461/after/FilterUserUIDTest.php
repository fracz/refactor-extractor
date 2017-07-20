<?php

/**
 * @file
 * Definition of Drupal\views\Tests\Comment\FilterUserUIDTest.
 */

namespace Drupal\views\Tests\Comment;

use Drupal\views\View;

/**
 * Tests the filter_comment_user_uid handler.
 *
 * The actual stuff is done in the parent class.
 */
class FilterUserUIDTest extends CommentTestBase {

  public static function getInfo() {
    return array(
      'name' => 'Comment: User UID Filter',
      'description' => 'Tests the user posted or commented filter handler.',
      'group' => 'Views Modules',
    );
  }

  /**
   * Override the view from the argument test case to remove the argument and
   * add filter with the uid as the value.
   */
  function view_comment_user_uid() {
    $view = parent::view_comment_user_uid();
    // Remove the argument.
    $view->set_item('default', 'argument', 'uid_touch', NULL);

    $options = array(
      'id' => 'uid_touch',
      'table' => 'node',
      'field' => 'uid_touch',
      'value' => array($this->loggedInUser->uid),
    );
    $view->add_item('default', 'filter', 'node', 'uid_touch', $options);

    return $view;
  }

  function testCommentUserUIDTest() {
    $view = $this->view_comment_user_uid();

    $this->executeView($view, array($this->account->uid));
    $result_set = array(
      array(
        'nid' => $this->node_user_posted->nid,
      ),
      array(
        'nid' => $this->node_user_commented->nid,
      ),
    );
    $this->column_map = array('nid' => 'nid');
    $this->assertIdenticalResultset($view, $result_set, $this->column_map);
  }

}