<?php

namespace Fragen\GitHub_Updater;

/**
 * Class Additions
 *
 * Add repos without GitHub Updater headers to GitHub Updater.
 * Uses JSON config data file and companion plugin.
 *
 * @package Fragen\GitHub_Updater
 * @author  Andy Fragen
 * @link    https://github.com/afragen/github-updater-additions
 */
class Additions {

	/**
	 * Holds instance of this object.
	 *
	 * @var
	 */
	private static $instance;

	/**
	 * Holds array of plugin/theme headers to add to GitHub Updater.
	 *
	 * @var
	 */
	public $add_to_github_updater = array();

	/**
	 * Singleton
	 *
	 * @return object
	 */
	public static function instance() {
		if ( ! isset( self::$instance ) ) {
			self::$instance = new self;
		}

		return self::$instance;
	}

	/**
	 * Register JSON config file.
	 *
	 * @param $config
	 * @param $repos
	 *
	 * @return bool
	 */
	public function register( $config, $repos, $type ) {
		if ( empty( $config ) ) {
			return false;
		}
		if ( null === ( $config = json_decode( $config, true ) ) ) {
			return false;
		}

		$this->add_headers( $config, $repos, $type );
	}

	/**
	 * Add GitHub Updater headers to plugins/themes via a filter hooks.
	 *
	 * @param $config
	 * @param $repos
	 * @param $type
	 */
	public function add_headers( $config, $repos, $type ) {
		$this->add_to_github_updater = array();
		foreach ( $config as $repo ) {
			$addition  = array();
			$additions[ $repo['slug'] ] = array();

			// Continue if repo not installed.
			if ( ! array_key_exists( $repo['slug'], $repos ) ) {
				continue;
			}

			if ( 'plugin' === $type ) {
				$additions[ $repo['slug'] ] = $repos[ $repo['slug'] ];
			}
			if ( 'theme' === $type ) {
				//$additions[ $repo['slug'] ] = array();
			}

			switch ( $repo['type'] ) {
				case 'github_plugin':
					$addition['GitHub Plugin URI'] = $repo['uri'];
					break;
				case 'bitbucket_plugin':
					$addition['Bitbucket Plugin URI'] = $repo['uri'];
					break;
				case 'gitlab_plugin':
					$addition['GitLab Plugin URI'] = $repo['uri'];
					break;
				case 'github_theme':
					$addition['slug']             = $repo['slug'];
					$addition['GitHub Theme URI'] = $repo['uri'];
					break;
				case 'bitbucket_theme':
					$addition['slug']                = $repo['slug'];
					$addition['Bitbucket Theme URI'] = $repo['uri'];
					break;
				case 'gitlab_theme':
					$addition['slug']             = $repo['slug'];
					$addition['GitLab Theme URI'] = $repo['uri'];
					break;
			}

			$this->add_to_github_updater[ $repo['slug'] ] = array_merge( $additions[ $repo['slug'] ], $addition );
		}
	}

}