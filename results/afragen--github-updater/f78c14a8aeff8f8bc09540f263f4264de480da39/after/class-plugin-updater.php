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
 * Update a WordPress plugin from a GitHub repo.
 *
 * @package GitHub_Plugin_Updater
 * @author  Andy Fragen
 * @author  Codepress
 * @link    https://github.com/codepress/github-plugin-updater
 */
class GitHub_Plugin_Updater extends GitHub_Updater {

	/**
	 * Store details for one GitHub-sourced plugin during the update procedure.
	 *
	 * @since 1.0.0
	 *
	 * @var stdClass
	 */
	protected $github_plugin;

	/**
	 * Constructor.
	 *
	 * @since 1.0.0
	 *
	 * @param array $config
	 */
	public function __construct() {

		// This MUST come before we get details about the plugins so the headers are correctly retrieved
		add_filter( 'extra_plugin_headers', array( $this, 'add_plugin_headers' ) );

		// Get details of GitHub-sourced plugins
		$this->config = $this->get_plugin_meta();
		if ( empty( $this->config ) ) return;
//fb($this->config);

		foreach ( $this->config as $plugin ) {

			$this->github_plugin = $plugin;
			$this->get_remote_info();
			$this->github_plugin->download_link = $this->construct_download_link();

//fb($this->github_plugin->slug);
//fb($this->github_plugin);

		}
fb($this->config);
		add_filter( 'pre_set_site_transient_update_plugins', array( $this, 'pre_set_site_transient_update_plugins' ) );
		add_filter( 'plugins_api', array( $this, 'get_remote_changes' ), 999, 3 );
		add_filter( 'upgrader_source_selection', array( $this, 'upgrader_source_selection' ), 10, 3 );
		add_action( 'http_request_args', array( $this, 'no_ssl_http_request_args' ) );
	}

	/**
	 * Call the GitHub API and return a json decoded body.
	 *
	 * @since 1.0.0
	 *
	 * @see http://developer.github.com/v3/
	 *
	 * @param string $url
	 *
	 * @return boolean|object
	 */
	protected function api( $url ) {
		$response = wp_remote_get( $this->get_api_url( $url ) );

		if ( is_wp_error( $response ) || wp_remote_retrieve_response_code( $response ) != '200' )
			return false;

//fb('api');
//fb($url);
//fb($response);
		return json_decode( wp_remote_retrieve_body( $response ) );
	}

	/**
	 * Return API url.
	 *
	 * @since 1.0.0
	 *
	 * @param string $endpoint
	 *
	 * @return string
	 */
	protected function get_api_url( $endpoint ) {
		$segments = array(
			'owner' => $this->github_plugin->owner,
			'repo'  => $this->github_plugin->repo,
		);

		/**
		 * Add or filter the available segments that are used to replace placeholders.
		 *
		 * @since 1.5.0
		 *
		 * @param array $segments List of segments.
		 */
		$segments = apply_filters( 'github_updater_api_segments', $segments );

		foreach ( $segments as $segment => $value ) {
			$endpoint = str_replace( '/:' . $segment, '/' . $value, $endpoint );
		}

		if ( ! empty( $this->github_plugin->access_token ) )
			$endpoint = add_query_arg( 'access_token', $this->github_plugin->access_token, $endpoint );

		// If a branch has been given, only check that for the remote info.
		// If it's not been given, GitHub will use the Default branch.
		if ( ! empty( $this->github_plugin->branch ) )
			$endpoint = add_query_arg( 'ref', $this->github_plugin->branch, $endpoint );

		return 'https://api.github.com' . $endpoint;
	}

	/**
	 * Read the remote plugin file.
	 *
	 * Uses a transient to limit the calls to the API.
	 *
	 * @since 1.0.0
	 */
	protected function get_remote_info() {
		$remote = get_site_transient( md5( $this->github_plugin->slug ) );
		if ( ! $remote ) {
			$remote = $this->api( '/repos/:owner/:repo/contents/' . basename( $this->github_plugin->slug ) );

			if ( $remote )
				set_site_transient( md5( $this->github_plugin->slug ), $remote, HOUR_IN_SECONDS );
		}

		if ( ! $remote ) return;

		preg_match( '/^[ \t\/*#@]*Version\:\s*(.*)$/im', base64_decode( $remote->content ), $matches );

		if ( ! empty( $matches[1] ) )
			$this->github_plugin->remote_version = $matches[1];

		$this->github_plugin->branch = $this->github_plugin->branch ? $this->github_plugin->branch : $this->get_default_branch( $remote );

		$this->github_plugin->newest_tag = $this->get_remote_tag();

//		return $remote;
	}

	/**
	 * Read the remote CHANGES.md file
	 *
	 * Uses a transient to limit calls to the API.
	 *
	 * @since 1.9.0
	 * @return base64 decoded CHANGES.md or false
	 */
	public function get_remote_changes( $false, $action, $response ) {
//fb('get_remote_changes');
		$this->github_plugin->sections =  array( 'changelog' => 'No changelog is available via GitHub Updater.' );

		if ( 'query_plugins' == $action ) return false;

		$url = '/repos/' . trailingslashit( $this->github_plugin->owner ) . trailingslashit( $this->github_plugin->repo ) . 'contents/CHANGES.md';

		$remote = get_site_transient( md5( $this->github_plugin->repo . 'changes' ) );
		if ( ! $remote ) {
			$remote = $this->api( $url );

			if ( $remote )
				set_site_transient( md5( $this->github_plugin->repo . 'changes' ), $remote, HOUR_IN_SECONDS );
		}

		if ( false != $remote ) {
			foreach ( $remote as $key => $value ) {
				if ( $key == 'content' ) {
					$this->github_plugin->sections = base64_decode( $value );
				}
			}
		}

		$response->sections = ( 'plugin_information' == $action ) ? $this->github_plugin->sections : array();
//fb($response);
		return $response;
	}

	/**
	 * Parse the remote info to find what the default branch is.
	 *
	 * If we've had to call this method, we know that a branch header has not been provided.
	 * As such the remote info was retrieved with a ?ref=... query argument.
	 *
	 * @since 1.5.0
	 * @param array API object
	 *
	 * @return string Default branch name.
	 */
	protected function get_default_branch( $response ) {
		// If we can't contact GitHub API, then assume a sensible default in case the non-API part of GitHub is working.
		if ( ! $response )
			return 'master';

		// Assuming we've got some remote info, parse the 'url' field to get the last bit of the ref query string
		$components = parse_url( $response->url, PHP_URL_QUERY );
		parse_str( $components );
		return $ref;
	}

	/**
	 * Parse the remote info to find most recent tag if tags exist
	 *
	 * Uses a transient to limit the calls to the API.
	 *
	 * @since 1.7.0
	 *
	 * @return string latest tag.
	 */
	protected function get_remote_tag() {
		$url = '/repos/' . trailingslashit( $this->github_plugin->owner ) . trailingslashit( $this->github_plugin->repo ) . 'tags';

		$response = get_site_transient( md5( $this->github_plugin->slug . 'tags' ) );

		if ( ! $response ) {
			$response = $this->api( $url );

			if ( $response )
				set_site_transient( md5( $this->github_plugin->slug . 'tags' ), $response, HOUR_IN_SECONDS );
		}

		// Sort and get latest tag
		$tags = array();
		if ( false !== $response )
			foreach ( $response as $num => $tag ) {
				if ( isset( $tag->name ) ) $tags[] = $tag->name;
			}

		if ( empty( $tags ) ) return false;  // no tags are present, exit early

		usort( $tags, 'version_compare' );

		// check and generate download link
		$newest_tag     = null;
		$newest_tag_key = key( array_slice( $tags, -1, 1, true ) );
		$newest_tag     = $tags[ $newest_tag_key ];

		return $newest_tag;
	}

	/**
	 * Construct $download_link
	 *
	 * @since 1.9.0
	 *
	 * @param stdClass plugin data
	 */
	protected function construct_download_link() {
		// just in case user started using tags then stopped.
		if ( $this->github_plugin->remote_version && $this->github_plugin->newest_tag && version_compare( $this->github_plugin->newest_tag, $this->github_plugin->remote_version, '>=' ) ) {
			$download_link = trailingslashit( $this->github_plugin->uri ) . 'archive/' . $this->github_plugin->newest_tag . '.zip';
		} else {
			$download_link = trailingslashit( $this->github_plugin->uri ) . 'archive/' . $this->github_plugin->branch . '.zip';
		}
		return $download_link;
	}

	/**
	 * Hook into pre_set_site_transient_update_plugins to update from GitHub.
	 *
	 * @since 1.0.0
	 *
	 * @param object $transient Original transient.
	 * @param stdClass plugin data
	 *
	 * @return $transient If all goes well, an updated transient that may include details of a plugin update.
	 */
	public function pre_set_site_transient_update_plugins( $transient ) {
		if ( empty( $transient->checked ) )
			return $transient;

		foreach ( (array) $this->config as $plugin ) {

			$remote_is_newer = ( 1 === version_compare( $plugin->remote_version, $plugin->local_version ) );

			if ( $remote_is_newer ) {
				$response = array(
					'slug'        => dirname( $plugin->slug ),
					'new_version' => $plugin->remote_version,
					'url'         => $plugin->uri,
					'package'     => $plugin->download_link,
				);

				$transient->response[ $plugin->slug ] = (object) $response;
			}
		}
		return $transient;
	}

	/**
	 * Rename the zip folder to be the same as the existing plugin folder.
	 *
	 * Github delivers zip files as <Repo>-<Branch>.zip
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
fb('upgrader_source_selection');
fb($this->github_plugin->repo);
fb($source);
		global $wp_filesystem;
		$update = array( 'update-selected', 'update-selected-themes', 'upgrade-theme', 'upgrade-plugin' );
		if ( isset( $source ) ) {
//			for ( $i = 0; $i < count( $this->config ); $i++ ) {
				if ( stristr( basename( $source ), $this->github_plugin->repo ) )
					$plugin = $this->github_plugin->repo;
//			}
		}

		// If there's no action set, or not one we recognise, abort
		if ( ! isset( $_GET['action'] ) || ! in_array( $_GET['action'], $update, true ) )
			return $source;

		// If the values aren't set, or it's not GitHub-sourced, abort
		if ( ! isset( $source, $remote_source, $plugin ) || false === stristr( basename( $source ), $plugin ) )
			return $source;

		$corrected_source = trailingslashit( $remote_source ) . trailingslashit( $plugin );
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
		$upgrader->skin->feedback( __( 'Unable to rename downloaded plugin.', 'github-updater' ) );
		return new WP_Error();
	}

}