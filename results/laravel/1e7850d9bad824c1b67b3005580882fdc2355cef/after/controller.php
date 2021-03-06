<?php namespace Laravel;

abstract class Controller {

	/**
	 * A stub method that will be called before every request to the controller.
	 *
	 * If a value is returned by the method, it will be halt the request process
	 * and will be considered the response to the request.
	 *
	 * @param  Request  $request
	 * @return mixed
	 */
	public function before(Request $request) {}

	/**
	 * Magic Method to handle calls to undefined functions on the controller.
	 */
	public function __call($method, $parameters) { return new Error('404'); }

}