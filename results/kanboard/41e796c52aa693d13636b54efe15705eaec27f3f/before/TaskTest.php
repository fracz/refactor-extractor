<?php

require_once __DIR__.'/Base.php';

use Model\Task;
use Model\Project;
use Model\ProjectPermission;
use Model\Category;
use Model\User;

class TaskTest extends Base
{
    public function testCreation()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));
        $this->assertEquals(1, $t->create(array('title' => 'Task #1', 'project_id' => 1, 'column_id' => 1)));

        $task = $t->getById(1);
        $this->assertEquals(1, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(1, $task['position']);
        $this->assertEquals('yellow', $task['color_id']);
        $this->assertEquals(time(), $task['date_creation']);
        $this->assertEquals(time(), $task['date_modification']);
        $this->assertEquals(0, $task['date_due']);

        $this->assertEquals(2, $t->create(array('title' => 'Task #2', 'project_id' => 1)));

        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(2, $task['position']);
        $this->assertEquals(time(), $task['date_creation']);
        $this->assertEquals(time(), $task['date_modification']);
        $this->assertEquals(0, $task['date_due']);

        $tasks = $t->getAll(1, 1);
        $this->assertNotEmpty($tasks);
        $this->assertTrue(is_array($tasks));
        $this->assertEquals(1, $tasks[0]['id']);
        $this->assertEquals(2, $tasks[1]['id']);

        $tasks = $t->getAll(1, 0);
        $this->assertEmpty($tasks);
    }

    public function testRemove()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'UnitTest')));
        $this->assertEquals(1, $t->create(array('title' => 'Task #1', 'project_id' => 1)));

        $this->assertTrue($t->remove(1));
        $this->assertFalse($t->remove(1234));
    }

    public function testGetOverdueTasks()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));
        $this->assertEquals(1, $t->create(array('title' => 'Task #1', 'project_id' => 1, 'date_due' => strtotime('-1 day'))));
        $this->assertEquals(2, $t->create(array('title' => 'Task #2', 'project_id' => 1, 'date_due' => strtotime('+1 day'))));
        $this->assertEquals(3, $t->create(array('title' => 'Task #3', 'project_id' => 1, 'date_due' => 0)));
        $this->assertEquals(4, $t->create(array('title' => 'Task #3', 'project_id' => 1)));

        $tasks = $t->getOverdueTasks();
        $this->assertNotEmpty($tasks);
        $this->assertTrue(is_array($tasks));
        $this->assertEquals(1, count($tasks));
        $this->assertEquals('Task #1', $tasks[0]['title']);
    }

    public function testMoveTaskWithColumnThatNotChange()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));

        $this->assertEquals(1, $t->create(array('title' => 'Task #1', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(2, $t->create(array('title' => 'Task #2', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(3, $t->create(array('title' => 'Task #3', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(4, $t->create(array('title' => 'Task #4', 'project_id' => 1, 'column_id' => 2)));
        $this->assertEquals(5, $t->create(array('title' => 'Task #5', 'project_id' => 1, 'column_id' => 2)));
        $this->assertEquals(6, $t->create(array('title' => 'Task #6', 'project_id' => 1, 'column_id' => 2)));
        $this->assertEquals(7, $t->create(array('title' => 'Task #7', 'project_id' => 1, 'column_id' => 3)));
        $this->assertEquals(8, $t->create(array('title' => 'Task #8', 'project_id' => 1, 'column_id' => 1)));

        // We move the task 3 to the column 3
        $this->assertTrue($t->movePosition(1, 3, 3, 2));

        // Check tasks position
        $task = $t->getById(1);
        $this->assertEquals(1, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $task = $t->getById(3);
        $this->assertEquals(3, $task['id']);
        $this->assertEquals(3, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        $task = $t->getById(5);
        $this->assertEquals(5, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $task = $t->getById(6);
        $this->assertEquals(6, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        $task = $t->getById(7);
        $this->assertEquals(7, $task['id']);
        $this->assertEquals(3, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        $task = $t->getById(8);
        $this->assertEquals(8, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(3, $task['position']);
    }

    public function testMoveTaskWithBadPreviousPosition()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));
        $this->assertEquals(1, $this->registry->shared('db')->table('tasks')->insert(array('title' => 'A', 'column_id' => 1, 'project_id' => 1, 'position' => 1)));

        // Both tasks have the same position
        $this->assertEquals(2, $this->registry->shared('db')->table('tasks')->insert(array('title' => 'B', 'column_id' => 2, 'project_id' => 1, 'position' => 1)));
        $this->assertEquals(3, $this->registry->shared('db')->table('tasks')->insert(array('title' => 'C', 'column_id' => 2, 'project_id' => 1, 'position' => 1)));

        // Move the first column to the last position of the 2nd column
        $this->assertTrue($t->movePosition(1, 1, 2, 3));

        // Check tasks position
        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        $task = $t->getById(3);
        $this->assertEquals(3, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $task = $t->getById(1);
        $this->assertEquals(1, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(3, $task['position']);
    }

    public function testMoveTaskTop()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));
        $this->assertEquals(1, $t->create(array('title' => 'Task #1', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(2, $t->create(array('title' => 'Task #2', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(3, $t->create(array('title' => 'Task #3', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(4, $t->create(array('title' => 'Task #4', 'project_id' => 1, 'column_id' => 1)));

        // Move the last task to the top
        $this->assertTrue($t->movePosition(1, 4, 1, 1));

        // Check tasks position
        $task = $t->getById(1);
        $this->assertEquals(1, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        $task = $t->getById(3);
        $this->assertEquals(3, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(4, $task['position']);

        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(1, $task['position']);
    }

    public function testMoveTaskBottom()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));
        $this->assertEquals(1, $t->create(array('title' => 'Task #1', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(2, $t->create(array('title' => 'Task #2', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(3, $t->create(array('title' => 'Task #3', 'project_id' => 1, 'column_id' => 1)));
        $this->assertEquals(4, $t->create(array('title' => 'Task #4', 'project_id' => 1, 'column_id' => 1)));

        // Move the last task to hte top
        $this->assertTrue($t->movePosition(1, 1, 1, 4));

        // Check tasks position
        $task = $t->getById(1);
        $this->assertEquals(1, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(4, $task['position']);

        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        $task = $t->getById(3);
        $this->assertEquals(3, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(3, $task['position']);
    }

    public function testMovePosition()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Project #1')));
        $counter = 1;
        $task_per_column = 5;

        foreach (array(1, 2, 3, 4) as $column_id) {

            for ($i = 1; $i <= $task_per_column; $i++, $counter++) {

                $task = array(
                    'title' => 'Task #'.$i.'-'.$column_id,
                    'project_id' => 1,
                    'column_id' => $column_id,
                    'owner_id' => 0,
                );

                $this->assertEquals($counter, $t->create($task));

                $task = $t->getById($counter);
                $this->assertNotFalse($task);
                $this->assertNotEmpty($task);
                $this->assertEquals($i, $task['position']);
            }
        }

        // We move task id #4, column 1, position 4 to the column 2, position 3
        $this->assertTrue($t->movePosition(1, 4, 2, 3));

        // We check the new position of the task
        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        // The tasks before have the correct position
        $task = $t->getById(3);
        $this->assertEquals(3, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        $task = $t->getById(7);
        $this->assertEquals(7, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        // The tasks after have the correct position
        $task = $t->getById(5);
        $this->assertEquals(5, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(4, $task['position']);

        $task = $t->getById(8);
        $this->assertEquals(8, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(4, $task['position']);

        // The number of tasks per column
        $this->assertEquals($task_per_column - 1, $t->countByColumnId(1, 1));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 2));
        $this->assertEquals($task_per_column, $t->countByColumnId(1, 3));
        $this->assertEquals($task_per_column, $t->countByColumnId(1, 4));

        // We move task id #1, column 1, position 1 to the column 4, position 6 (last position)
        $this->assertTrue($t->movePosition(1, 1, 4, $task_per_column + 1));

        // We check the new position of the task
        $task = $t->getById(1);
        $this->assertEquals(1, $task['id']);
        $this->assertEquals(4, $task['column_id']);
        $this->assertEquals($task_per_column + 1, $task['position']);

        // The tasks before have the correct position
        $task = $t->getById(20);
        $this->assertEquals(20, $task['id']);
        $this->assertEquals(4, $task['column_id']);
        $this->assertEquals($task_per_column, $task['position']);

        // The tasks after have the correct position
        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        // The number of tasks per column
        $this->assertEquals($task_per_column - 2, $t->countByColumnId(1, 1));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 2));
        $this->assertEquals($task_per_column, $t->countByColumnId(1, 3));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 4));

        // Our previous moved task should stay at the same place
        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        // Test wrong position number
        $this->assertFalse($t->movePosition(1, 2, 3, 0));
        $this->assertFalse($t->movePosition(1, 2, 3, -2));

        // Wrong column
        $this->assertFalse($t->movePosition(1, 2, 22, 2));

        // Test position greater than the last position
        $this->assertTrue($t->movePosition(1, 11, 1, 22));

        $task = $t->getById(11);
        $this->assertEquals(11, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals($t->countByColumnId(1, 1), $task['position']);

        $task = $t->getById(5);
        $this->assertEquals(5, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals($t->countByColumnId(1, 1) - 1, $task['position']);

        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        $this->assertEquals($task_per_column - 1, $t->countByColumnId(1, 1));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 2));
        $this->assertEquals($task_per_column - 1, $t->countByColumnId(1, 3));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 4));

        // Our previous moved task should stay at the same place
        $task = $t->getById(4);
        $this->assertEquals(4, $task['id']);
        $this->assertEquals(2, $task['column_id']);
        $this->assertEquals(3, $task['position']);

        // Test moving task to position 1
        $this->assertTrue($t->movePosition(1, 14, 1, 1));

        $task = $t->getById(14);
        $this->assertEquals(14, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(1, $task['position']);

        $task = $t->getById(2);
        $this->assertEquals(2, $task['id']);
        $this->assertEquals(1, $task['column_id']);
        $this->assertEquals(2, $task['position']);

        $this->assertEquals($task_per_column, $t->countByColumnId(1, 1));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 2));
        $this->assertEquals($task_per_column - 2, $t->countByColumnId(1, 3));
        $this->assertEquals($task_per_column + 1, $t->countByColumnId(1, 4));
    }

    public function testFilter()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'test1')));
        $this->assertEquals(1, $t->create(array('title' => 'test a', 'project_id' => 1, 'column_id' => 3, 'owner_id' => 1, 'description' => 'biloute')));
        $this->assertEquals(2, $t->create(array('title' => 'test b', 'project_id' => 1, 'column_id' => 2, 'owner_id' => 2, 'description' => 'toto et titi sont dans un bateau')));

        $tasks = $t->find(array(array('column' => 'project_id', 'operator' => 'eq', 'value' => '1')));
        $this->assertNotFalse($tasks);
        $this->assertEquals(2, count($tasks));
        $this->assertEquals(1, $tasks[0]['id']);
        $this->assertEquals(2, $tasks[1]['id']);

        $tasks = $t->find(array(
            array('column' => 'project_id', 'operator' => 'eq', 'value' => '1'),
            array('column' => 'owner_id', 'operator' => 'eq', 'value' => '2'),
        ));
        $this->assertEquals(1, count($tasks));
        $this->assertEquals(2, $tasks[0]['id']);

        $tasks = $t->find(array(
            array('column' => 'project_id', 'operator' => 'eq', 'value' => '1'),
            array('column' => 'title', 'operator' => 'like', 'value' => '%b%'),
        ));
        $this->assertEquals(1, count($tasks));
        $this->assertEquals(2, $tasks[0]['id']);

        // Condition with OR
        $search = 'bateau';
        $filters = array(
            array('column' => 'project_id', 'operator' => 'eq', 'value' => 1),
            'or' => array(
                array('column' => 'title', 'operator' => 'like', 'value' => '%'.$search.'%'),
                array('column' => 'description', 'operator' => 'like', 'value' => '%'.$search.'%'),
            )
        );

        $tasks = $t->find($filters);
        $this->assertEquals(1, count($tasks));
        $this->assertEquals(2, $tasks[0]['id']);

        $search = 'toto et titi';
        $filters = array(
            array('column' => 'project_id', 'operator' => 'eq', 'value' => 1),
            'or' => array(
                array('column' => 'title', 'operator' => 'like', 'value' => '%'.$search.'%'),
                array('column' => 'description', 'operator' => 'like', 'value' => '%'.$search.'%'),
            )
        );

        $tasks = $t->find($filters);
        $this->assertEquals(1, count($tasks));
        $this->assertEquals(2, $tasks[0]['id']);

        $search = 'john';
        $filters = array(
            array('column' => 'project_id', 'operator' => 'eq', 'value' => 1),
            'or' => array(
                array('column' => 'title', 'operator' => 'like', 'value' => '%'.$search.'%'),
                array('column' => 'description', 'operator' => 'like', 'value' => '%'.$search.'%'),
            )
        );

        $tasks = $t->find($filters);
        $this->assertEquals(0, count($tasks));
    }

    public function testDuplicateToTheSameProject()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);
        $c = new Category($this->registry);

        // We create a task and a project
        $this->assertEquals(1, $p->create(array('name' => 'test1')));

        // Some categories
        $this->assertNotFalse($c->create(array('name' => 'Category #1', 'project_id' => 1)));
        $this->assertNotFalse($c->create(array('name' => 'Category #2', 'project_id' => 1)));
        $this->assertTrue($c->exists(1, 1));
        $this->assertTrue($c->exists(2, 1));

        $this->assertEquals(1, $t->create(array('title' => 'test', 'project_id' => 1, 'column_id' => 3, 'owner_id' => 1, 'category_id' => 2)));

        $task = $t->getById(1);
        $this->assertNotEmpty($task);
        $this->assertEquals(1, $task['position']);

        // We duplicate our task
        $this->assertEquals(2, $t->duplicateSameProject($task));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_CREATE));

        // Check the values of the duplicated task
        $task = $t->getById(2);
        $this->assertNotEmpty($task);
        $this->assertEquals(Task::STATUS_OPEN, $task['is_active']);
        $this->assertEquals(1, $task['project_id']);
        $this->assertEquals(1, $task['owner_id']);
        $this->assertEquals(2, $task['category_id']);
        $this->assertEquals(3, $task['column_id']);
        $this->assertEquals(2, $task['position']);
    }

    public function testDuplicateToAnotherProject()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);
        $c = new Category($this->registry);

        // We create 2 projects
        $this->assertEquals(1, $p->create(array('name' => 'test1')));
        $this->assertEquals(2, $p->create(array('name' => 'test2')));

        $this->assertNotFalse($c->create(array('name' => 'Category #1', 'project_id' => 1)));
        $this->assertTrue($c->exists(1, 1));

        // We create a task
        $this->assertEquals(1, $t->create(array('title' => 'test', 'project_id' => 1, 'column_id' => 2, 'owner_id' => 1, 'category_id' => 1)));
        $task = $t->getById(1);

        // We duplicate our task to the 2nd project
        $this->assertEquals(2, $t->duplicateToAnotherProject(2, $task));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_CREATE));

        // Check the values of the duplicated task
        $task = $t->getById(2);
        $this->assertNotEmpty($task);
        $this->assertEquals(1, $task['owner_id']);
        $this->assertEquals(0, $task['category_id']);
        $this->assertEquals(5, $task['column_id']);
        $this->assertEquals(1, $task['position']);
        $this->assertEquals(2, $task['project_id']);
        $this->assertEquals('test', $task['title']);
    }

    public function testMoveToAnotherProject()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);
        $pp = new ProjectPermission($this->registry);
        $user = new User($this->registry);

        // We create a regular user
        $user->create(array('username' => 'unittest1', 'password' => 'unittest'));
        $user->create(array('username' => 'unittest2', 'password' => 'unittest'));

        // We create 2 projects
        $this->assertEquals(1, $p->create(array('name' => 'test1')));
        $this->assertEquals(2, $p->create(array('name' => 'test2')));

        // We create a task
        $this->assertEquals(1, $t->create(array('title' => 'test', 'project_id' => 1, 'column_id' => 1, 'owner_id' => 1, 'category_id' => 10, 'position' => 333)));
        $this->assertEquals(2, $t->create(array('title' => 'test2', 'project_id' => 1, 'column_id' => 1, 'owner_id' => 3, 'category_id' => 10, 'position' => 333)));

        // We duplicate our task to the 2nd project
        $task = $t->getById(1);
        $this->assertEquals(1, $t->moveToAnotherProject(2, $task));
        //$this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_CREATE));

        // Check the values of the duplicated task
        $task = $t->getById(1);
        $this->assertNotEmpty($task);
        $this->assertEquals(1, $task['owner_id']);
        $this->assertEquals(0, $task['category_id']);
        $this->assertEquals(2, $task['project_id']);
        $this->assertEquals(5, $task['column_id']);
        $this->assertEquals(1, $task['position']);
        $this->assertEquals('test', $task['title']);

        // We allow only one user on the second project
        $this->assertTrue($pp->allowUser(2, 2));

        // The owner should be reseted
        $task = $t->getById(2);
        $this->assertEquals(2, $t->moveToAnotherProject(2, $task));

        $task = $t->getById(2);
        $this->assertNotEmpty($task);
        $this->assertEquals(0, $task['owner_id']);
    }

    public function testEvents()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);

        // We create a project
        $this->assertEquals(1, $p->create(array('name' => 'test')));

        // We create task
        $this->assertEquals(1, $t->create(array('title' => 'test', 'project_id' => 1, 'column_id' => 1)));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_CREATE));

        // We update a task
        $this->assertTrue($t->update(array('title' => 'test2', 'id' => 1)));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_UPDATE));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_CREATE_UPDATE));

        // We close our task
        $this->assertTrue($t->close(1));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_CLOSE));

        // We open our task
        $this->assertTrue($t->open(1));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_OPEN));

        // We change the column of our task
        $this->assertTrue($t->movePosition(1, 1, 2, 1));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_MOVE_COLUMN));

        // We change the position of our task
        $this->assertEquals(2, $t->create(array('title' => 'test 2', 'project_id' => 1, 'column_id' => 2)));
        $this->assertTrue($t->movePosition(1, 1, 2, 2));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_MOVE_POSITION));

        // We change the column and the position of our task
        $this->assertTrue($t->movePosition(1, 1, 1, 1));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_MOVE_COLUMN));

        // We change the assignee
        $this->assertTrue($t->update(array('owner_id' => 1, 'id' => 1)));
        $this->assertTrue($this->registry->shared('event')->isEventTriggered(Task::EVENT_ASSIGNEE_CHANGE));
    }
}