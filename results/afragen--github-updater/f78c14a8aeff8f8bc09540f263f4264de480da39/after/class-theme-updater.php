<?php
/**
 * GitHub Updater
 *
 * @package   GitHub_Updater
 * @author    Andy Fragen
 * @license   GPL-2.0+
 * @link      https://github.com/afragen/github-updater
 */

/**
 * Update a WordPress theme from a GitHub repo.
 *
 * @package   GitHub_Theme_Updater
 * @author    Andy Fragen
 * @author    Seth Carstens
 * @link      https://github.com/scarstens/Github-Theme-Updater
 * @author    UCF Web Communications
 * @link      https://github.com/UCF/Theme-Updater
 */
class GitHub_Theme_Updater extends GitHub_Updater {

	public function __construct() {

		// This MUST come before we get details about the plugins so the headers are correctly retrieved
		add_filter( 'extra_theme_headers', array( $this, 'add_theme_headers' ) );

		// Get details of GitHub-sourced themes
		$this->config = $this->get_themes_meta();
		if ( empty( $this->config ) ) return;

		if ( ! empty($_GET['action'] ) && ( $_GET['action'] == 'do-core-reinstall' || $_GET['action'] == 'do-core-upgrade') ); else {
			add_filter( 'pre_set_site_transient_update_themes', array( $this, 'pre_set_site_transient_update_themes' ) );
		}

		add_filter( 'upgrader_source_selection', array( $this, 'upgrader_source_selection' ), 10, 3 );
		add_action( 'http_request_args', array( $this, 'no_ssl_http_request_args' ) );
	}

	/**
	 * Call the GitHub API and return a json decoded body.
	 *
	 * @since 1.0.0
	 *
	 * @param string $url
	 * @see http://developer.github.com/v3/
	 * @return boolean|object
	 */
	protected function api( $url ) {

		$response = wp_remote_get( $url );

		if ( is_wp_error( $response ) || wp_remote_retrieve_response_code( $response ) != '200' )
			return false;

		return json_decode( wp_remote_retrieve_body( $response ) );
	}

	/**
	 * Reads the remote plugin file.
	 *
	 * Uses a transient to limit the calls to the API.
	 *
	 * @since 1.0.0
	 */
	protected function get_remote_info( $url ) {

		$remote = get_site_transient( md5( $url . 'theme' ) ) ;

		if ( ! $remote ) {
			$remote = $this->api( $url );

			if ( $remote )
				set_site_transient( md5( $url . 'theme' ), $remote, HOUR_IN_SECONDS );
		}

		return $remote;
	}

	/**
	 * Hook into pre_set_site_transient_update_themes to update from GitHub.
	 *
	 * Finds newest tag and compares to current tag
	 *
	 * @since 1.0.0
	 *
	 * @param array $data
	 * @return array|object
	 */
	public function pre_set_site_transient_update_themes( $data ){

		foreach ( $this->config as $theme => $theme_data ) {
			if ( empty( $theme_data['api'] ) ) continue;
			$url      = trailingslashit( $theme_data['api'] ) . 'tags';
			$response = $this->get_remote_info( $url );

			// Sort and get latest tag
			$tags = array();
			if ( false !== $response )
				foreach ( $response as $num => $tag ) {
					if ( isset( $tag->name ) ) $tags[] = $tag->name;
				}

			// if no tag set or no version number then abort
			if ( empty( $tags ) || is_null( $theme_data['version'] ) ) return false;
			usort( $tags, 'version_compare' );

			// check and generate download link
			$newest_tag     = null;
			$newest_tag_key = key( array_slice( $tags, -1, 1, true ) );
			$newest_tag     = $tags[ $newest_tag_key ];

			$download_link = trailingslashit( $theme_data['uri'] ) . trailingslashit( 'archive' ) . $newest_tag . '.zip';

			// setup update array to append version info
			$update = array();
			$update['new_version'] = $newest_tag;
			$update['url']         = $theme_data['uri'];
			$update['package']     = $download_link;

			if ( version_compare( $theme_data['version'],  $newest_tag, '>=' ) ) {
				// up-to-date!
				$data->up_to_date[ $theme_data['theme_key'] ]['rollback'] = $tags;
				$data->up_to_date[ $theme_data['theme_key'] ]['response'] = $update;
			} else {
				$data->response[ $theme_data['theme_key'] ] = $update;
			}
		}
		return $data;
	}

	/**
	 * Rename the zip folder to be the same as the existing theme folder.
	 *
	 * Github delivers zip files as <Repo>-<Tag>.zip
	 *
	 * @since 1.0.0
	 *
	 * @global WP_Filesystem $wp_filesystem
	 *
	 * @param string $source
	 * @param string $remote_source Optional.
	 * @param object $upgrader      Optional.
	 *
	 * @return string
	 */
	public function upgrader_source_selection( $source, $remote_source = null, $upgrader = null ) {
fb($source);
		global $wp_filesystem;
		$update = array( 'update-selected', 'update-selected-themes', 'upgrade-theme', 'upgrade-plugin' );

		if ( isset( $source, $this->config['theme'] ) ) {
			for ( $i = 0; $i < count( $this->config['theme'] ); $i++ ) {
				if ( stristr( basename( $source ), $this->config['theme'][$i] ) )
					$theme = $this->config['theme'][$i];
			}
		}

		// If there's no action set, or not one we recognise, abort
		if ( ! isset( $_GET['action'] ) || ! in_array( $_GET['action'], $update, true ) )
			return $source;

		// If the values aren't set, or it's not GitHub-sourced, abort
		if ( ! isset( $source, $remote_source, $theme ) || false === stristr( basename( $source ), $theme ) )
			return $source;

		$corrected_source = trailingslashit( $remote_source ) . trailingslashit( $theme );
		$upgrader->skin->feedback(
			sprintf(
				__( 'Renaming %s to %s&#8230;', 'github-updater' ),
				'<span class="code">' . basename( $source ) . '</span>',
				'<span class="code">' . basename( $corrected_source ) . '</span>'
			)
		);

		// If we can rename, do so and return the new name
		if ( $wp_filesystem->move( $source, $corrected_source, true ) ) {
			$upgrader->skin->feedback( __( 'Rename successful&#8230;', 'github-updater' ) );
			return $corrected_source;
		}

		// Otherwise, return an error
		$upgrader->skin->feedback( __( 'Unable to rename downloaded theme.', 'github-updater' ) );
		return new WP_Error();
	}

}