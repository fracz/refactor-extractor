<?php namespace GrahamCampbell\BootstrapCMS\Tests\Controllers;

/**
 * This file is part of Bootstrap CMS by Graham Campbell.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * @package    Bootstrap-CMS
 * @author     Graham Campbell
 * @license    GNU AFFERO GENERAL PUBLIC LICENSE
 * @copyright  Copyright (C) 2013  Graham Campbell
 * @link       https://github.com/GrahamCampbell/Bootstrap-CMS
 */

use Binput;
use Mockery;
use Validator;

class CommentControllerActionTest extends ResourcefulActionTestCase {

    use CommentControllerSetupTrait;

    protected function storeMocking() {
        $provider = $this->provider;
        $provider::shouldReceive('create')
            ->once()->andReturn($this->mock);
        $this->mock->shouldReceive('getUserName')->once();
    }

    protected function storeCall() {
        $this->call('POST', $this->getPath($this->getUid().'/comments'), array(), array(), array('HTTP_X-Requested-With' => 'XMLHttpRequest'));
    }

    protected function storeAssertions() {
        $this->assertResponseOk();
    }

    protected function storeFailsCall() {
        $this->setExpectedException('Symfony\Component\HttpKernel\Exception\HttpException');
        $this->storeCall();
    }

    protected function storeFailsAssertions() {
        $this->assertResponseStatus(400);
    }

    protected function updateSetup() {
        Validator::shouldReceive('make')->twice()
            ->andReturn(Mockery::mock(array('passes' => true, 'fails' => false, 'errors' => array())));
    }

    protected function updateMocking() {
        $provider = $this->provider;
        $provider::shouldReceive('find')
            ->with($this->getUid())->once()->andReturn($this->mock);
        $this->mock->shouldReceive('update')->once();
    }

    protected function updateCall() {
        $this->call('PATCH', $this->getPath($this->getUid().'/comments/'.$this->getUid()), array(), array(), array('HTTP_X-Requested-With' => 'XMLHttpRequest'));
    }

    protected function updateAssertions() {
        $this->assertResponseOk();
    }

    protected function updateFailsCall() {
        $this->setExpectedException('Symfony\Component\HttpKernel\Exception\HttpException');
        $this->updateCall();
    }

    protected function updateFailsAssertions() {
        $this->assertResponseStatus(400);
    }

    protected function destroyMocking() {
        $provider = $this->provider;
        $provider::shouldReceive('find')
            ->with($this->getUid())->once()->andReturn($this->mock);
        $this->mock->shouldReceive('delete')->once();
    }

    protected function destroyCall() {
        $this->call('DELETE', $this->getPath($this->getUid().'/comments/'.$this->getUid()), array(), array(), array('HTTP_X-Requested-With' => 'XMLHttpRequest'));
    }

    protected function destroyAssertions() {
        $this->assertResponseOk();
    }
}