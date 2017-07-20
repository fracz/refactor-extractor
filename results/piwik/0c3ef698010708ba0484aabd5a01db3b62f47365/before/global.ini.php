; <?php exit; ?> DO NOT REMOVE THIS LINE
[superuser]
login			= root
password		=

[database]
host			=
username		=
password		=
dbname			=
tables_prefix	=
port			= 3306
adapter			= PDO_MYSQL ; PDO_MYSQL or MYSQLI

[database_tests]
host 			= localhost
username 		= root
password 		=
dbname			= piwik_tests
tables_prefix	= piwiktests_
port			= 3306
adapter 		= PDO_MYSQL


[Plugins]
Plugins[] 		= CorePluginsAdmin
Plugins[] 		= CoreAdminHome
Plugins[] 		= CoreHome
Plugins[] 		= API
Plugins[] 		= Widgetize
Plugins[] 		= LanguagesManager
Plugins[] 		= Actions
Plugins[] 		= Dashboard
Plugins[] 		= Referers
Plugins[] 		= UserSettings

Plugins[] 		= UserCountry
Plugins[] 		= VisitsSummary
Plugins[] 		= VisitFrequency
Plugins[] 		= VisitTime
Plugins[] 		= VisitorInterest
Plugins[] 		= ExampleAPI
Plugins[] 		= ExamplePlugin
Plugins[]		= ExampleRssWidget
Plugins[] 		= ExampleFeedburner
Plugins[] 		= ExampleRssWidget
Plugins[] 		= Provider
Plugins[]		= Feedback

Plugins[] 		= Login
Plugins[] 		= UsersManager
Plugins[] 		= SitesManager
Plugins[] 		= Installation

[PluginsInstalled]
PluginsInstalled[] = Login
PluginsInstalled[] = CoreAdminHome
PluginsInstalled[] = UsersManager
PluginsInstalled[] = SitesManager
PluginsInstalled[] = Installation

[Plugins_Tracker]


[Debug]
; if set to true, the archiving process will always be triggered, even if the archive has already been computed
; this is useful when making changes to the archiving code so we can force the archiving process
always_archive_data = false

; if set to true, all the SQL queries will be recorded by the profiler
; and a profiling summary will be printed at the end of the request
enable_sql_profiler = false

[General]
; Time in seconds after which an archive will be computed again.
; This setting is used only for today's statistics.
; Defaults to 10 seconds so that by default, Piwik provides real time reporting.
time_before_archive_considered_outdated = 10

; When loading piwik interface, we redirect the user to 'yesterday' statistics by default
; Possible values: yesterday, today, or any YYYY-MM-DD
default_day = yesterday

; When loading piwik interface, Piwik will load by default the CoreHome module
; You can override the setting to force the user to login.
; This is useful when you have some websites view "anonymous" access but you want to
; force users to login instead of viewing the first anonymous website available
default_module_login = false

; When loading the piwik interface in the browser (as opposed to from the PHP-CLI client)
; should we launch the archiving process if the archives have not yet been processed?
; You want to set it to false when triggering the archiving is done through a crontab,
; so that your users do not trigger archiving in their browser when this is not expected
enable_browser_archiving_triggering = true

; character used to automatically create categories in the "Action" "Downloads" reports
; for example a URL like "example.com/blog/development/first-post" will create
; the page first-post in the subcategory development which belongs to the blog category
action_category_delimiter = /

; if you want all your users to use Piwik in only one language, disable the LanguagesManager
; plugin, and set this default_language (users won't see the language drop down)
default_language = en

; default sorting order used by all datatables (desc or asc)
dataTable_default_sort_order = desc

; default number of elements in the datatable
dataTable_default_limit = 10

; PHP minimum required version (minimum requirement known to date = ->newInstanceArgs)
minimum_php_version = 5.1.3

; MySQL minimum required version
minimum_mysql_version = 4.1

minimum_memory_limit = 128

[Tracker]
; set to 0 if you want to stop tracking the visitors. Useful if you need to stop all the connections on the DB.
record_statistics			= 1

; this action name is used when the javascript variable piwik_action_name is not specified in the piwik javascript code, and when the URL has no path.
default_action_name 		= index

; length of a visit in seconds. If a visitor comes back on the website visit_standard_length seconds after his last page view, it will be recorded as a new visit
visit_standard_length       = 1800

; visitors that stay on the website and view only one page will be considered staying 10 seconds
default_time_one_page_visit = 10

; variable name used to specify a download link
; Example: '/piwik.php?idsite=1&download=http://piwik.org/piwik.zip' will redirect to 'http://piwik.org/piwik.zip'
download_url_var_name 		= download

; variable name used to specify a link to an external website
; Example: '/piwik.php?idsite=1&link=http://piwik.org/' will redirect to 'http://piwik.org/'
outlink_url_var_name		= link

; variable that contains the name of the download or the outlink to redirect to
; Example: '/piwik.php?idsite=1&download=http://piwik.org/piwik.zip&name=Piwik last version'
download_outlink_name_var   = name

; variable name to track any campaign, for example CPC campaign
; Example: If a visitor first visits 'index.php?piwik_campaign=Adwords-CPC' then it will be counted as a campaign referer named 'Adwords-CPC'
campaign_var_name			= piwik_campaign

; variable name to track any campaign keyword
; Example: If a visitor first visits 'index.php?piwik_campaign=Adwords-CPC&piwik_kwd=My killer keyword' then it will be counted as a campaign referer named 'Adwords-CPC' with the keyword 'My killer keyword'
campaign_keyword_var_name	= piwik_kwd

; name of the cookie used to store the visitor information
cookie_name	= piwik_visitor

[log]

;possible values for log: screen, database, file
; normal messages
logger_message[]		= screen
logger_error[]			= screen
logger_exception[]		= screen

; all calls to the API (method name, parameters, execution time, caller IP, etc.)
;logger_api_call[]		= file

[log_tests]
logger_message[]		= screen
logger_api_call[]		= screen
logger_error[]			= screen
logger_exception[]		= screen

[path]
log				= tmp/logs/

[smarty]
; the list of directories in which to look for templates
template_dir[]	= plugins
template_dir[]	= themes/default
template_dir[]	= themes

plugins_dir[] 	= libs/Smarty/plugins
plugins_dir[]	= core/SmartyPlugins

compile_dir		= tmp/templates_c
cache_dir		= tmp/cache

; error reporting inside Smarty
error_reporting = E_ALL|E_NOTICE

; allow smarty debugging using {debug}
debugging		= true