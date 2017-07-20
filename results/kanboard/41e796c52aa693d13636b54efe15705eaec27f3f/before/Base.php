<?php

namespace Model;

use Core\Event;
use Core\Tool;
use Core\Registry;
use PicoDb\Database;

/**
 * Base model class
 *
 * @package  model
 * @author   Frederic Guillot
 *
 * @property \Model\Acl                $acl
 * @property \Model\Action             $action
 * @property \Model\Authentication     $authentication
 * @property \Model\Board              $board
 * @property \Model\Category           $category
 * @property \Model\Comment            $comment
 * @property \Model\Color              $color
 * @property \Model\Config             $config
 * @property \Model\DateParser         $dateParser
 * @property \Model\File               $file
 * @property \Model\LastLogin          $lastLogin
 * @property \Model\Notification       $notification
 * @property \Model\Project            $project
 * @property \Model\ProjectPermission  $projectPermission
 * @property \Model\SubTask            $subTask
 * @property \Model\Task               $task
 * @property \Model\TaskExport         $taskExport
 * @property \Model\TaskHistory        $taskHistory
 * @property \Model\TaskValidator      $taskValidator
 * @property \Model\User               $user
 * @property \Model\Webhook            $webhook
 */
abstract class Base
{
    /**
     * Database instance
     *
     * @access protected
     * @var \PicoDb\Database
     */
    protected $db;

    /**
     * Event dispatcher instance
     *
     * @access public
     * @var \Core\Event
     */
    public $event;

    /**
     * Registry instance
     *
     * @access protected
     * @var \Core\Registry
     */
    protected $registry;

    /**
     * Constructor
     *
     * @access public
     * @param  \Core\Registry  $registry  Registry instance
     */
    public function __construct(Registry $registry)
    {
        $this->registry = $registry;
        $this->db = $this->registry->shared('db');
        $this->event = $this->registry->shared('event');
    }

    /**
     * Load automatically models
     *
     * @access public
     * @param  string $name Model name
     * @return mixed
     */
    public function __get($name)
    {
        return Tool::loadModel($this->registry, $name);
    }
}