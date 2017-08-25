<?php namespace GrahamCampbell\BootstrapCMS\Tests;

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

class BasicTest extends TestCase {

    public function testBase() {
        $this->call('GET', '/');

        $this->assertRedirectedToRoute('pages.show', array('pages' => 'home'));
    }

    public function testBlog() {
        $this->call('GET', 'blog');

        $this->assertRedirectedToRoute('blog.posts.index');
    }
}