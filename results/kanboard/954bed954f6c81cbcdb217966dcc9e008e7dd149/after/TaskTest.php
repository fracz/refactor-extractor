<?php

require_once __DIR__.'/Base.php';

use Model\Task;
use Model\Project;
use Model\Category;
use Model\User;

class TaskTest extends Base
{
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

    public function testExport()
    {
        $t = new Task($this->registry);
        $p = new Project($this->registry);
        $c = new Category($this->registry);

        $this->assertEquals(1, $p->create(array('name' => 'Export Project')));
        $this->assertNotFalse($c->create(array('name' => 'Category #1', 'project_id' => 1)));
        $this->assertNotFalse($c->create(array('name' => 'Category #2', 'project_id' => 1)));
        $this->assertNotFalse($c->create(array('name' => 'Category #3', 'project_id' => 1)));

        for ($i = 1; $i <= 100; $i++) {

            $task = array(
                'title' => 'Task #'.$i,
                'project_id' => 1,
                'column_id' => rand(1, 3),
                'creator_id' => rand(0, 1),
                'owner_id' => rand(0, 1),
                'color_id' => rand(0, 1) === 0 ? 'green' : 'purple',
                'category_id' => rand(0, 3),
                'date_due' => array_rand(array(0, date('Y-m-d'), date('Y-m-d', strtotime('+'.$i.'day')))),
                'score' => rand(0, 21)
            );

            $this->assertEquals($i, $t->create($task));
        }

        $rows = $t->export(1, strtotime('-1 day'), strtotime('+1 day'));
        $this->assertEquals($i, count($rows));
        $this->assertEquals('Task Id', $rows[0][0]);
        $this->assertEquals(1, $rows[1][0]);
        $this->assertEquals('Task #'.($i - 1), $rows[$i - 1][11]);
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

    public function testDateFormat()
    {
        $t = new Task($this->registry);

        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('2014-03-05', 'Y-m-d')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('2014_03_05', 'Y_m_d')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('05/03/2014', 'd/m/Y')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('03/05/2014', 'm/d/Y')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('3/5/2014', 'm/d/Y')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('5/3/2014', 'd/m/Y')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->getValidDate('5/3/14', 'd/m/y')));
        $this->assertEquals(0, $t->getValidDate('5/3/14', 'd/m/Y'));
        $this->assertEquals(0, $t->getValidDate('5-3-2014', 'd/m/Y'));

        $this->assertEquals('2014-03-05', date('Y-m-d', $t->parseDate('2014-03-05')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->parseDate('2014_03_05')));
        $this->assertEquals('2014-03-05', date('Y-m-d', $t->parseDate('03/05/2014')));
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
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_CREATE));

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
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_CREATE));

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
        //$this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_CREATE));

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
        $this->assertTrue($p->allowUser(2, 2));

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
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_CREATE));

        // We update a task
        $this->assertTrue($t->update(array('title' => 'test2', 'id' => 1)));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_UPDATE));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_CREATE_UPDATE));

        // We close our task
        $this->assertTrue($t->close(1));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_CLOSE));

        // We open our task
        $this->assertTrue($t->open(1));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_OPEN));

        // We change the column of our task
        $this->assertTrue($t->movePosition(1, 1, 2, 1));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_MOVE_COLUMN));

        // We change the position of our task
        $this->assertEquals(2, $t->create(array('title' => 'test 2', 'project_id' => 1, 'column_id' => 2)));
        $this->assertTrue($t->movePosition(1, 1, 2, 2));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_MOVE_POSITION));

        // We change the column and the position of our task
        $this->assertTrue($t->movePosition(1, 1, 1, 1));
        $this->assertTrue($this->registry->event->isEventTriggered(Task::EVENT_MOVE_COLUMN));
    }
}