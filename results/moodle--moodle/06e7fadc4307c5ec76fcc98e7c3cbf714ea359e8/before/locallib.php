<?php
/**
 *
 * Rest library
 *
 * @author Jerome Mouneyrac, Ferran Recio, David Castro Garcia
 */

/**
 *
 * @author Jerome Mouneyrac
 * @global object $CFG
 * @param string $rest_arguments example: /mod/forum/get_discussion
 * @return string xml object
 */
function call_moodle_function ($rest_arguments) {
    global $CFG, $USER;

///REST params conversion
    $functionname = substr($rest_arguments,strrpos($rest_arguments,"/")+1); //retrieve the function name (it's located after the last '/') in $rest_arguments
                                                                            //$rest_argument
    $apipath = substr($rest_arguments,0, strlen($rest_arguments) - strlen($functionname)); //api path is the other part of $rest_arguments

    $classname = str_replace('/', '_', $apipath); // convert '/' into '_' (e.g. /mod/forum/ => _mod_forum_)
    $classname = substr($classname,1, strlen($classname) - 1); //remove first _ (e.g. _mod_forum => mod_forum)
    $classname .= 'external';

/// Authentication process
/// TODO: this use a fake token => need to implement token generation
    $token = optional_param('token',null,PARAM_ALPHANUM);
    if (empty($token)) {
        if ($functionname != 'tmp_get_token') {
            throw new moodle_exception('identifyfirst');
        } else {
            /// TODO: authentication + token generation need to be implemented
            if (optional_param('username',null,PARAM_ALPHANUM) == 'wsuser' && optional_param('password',null,PARAM_ALPHANUM) == 'wspassword') {
                return '465465465468468464';
            } else {
                throw new moodle_exception('wrongusernamepassword');
            }
        }
    } else {
        /// TODO: following function will need to be modified
        $user = mock_check_token($token);
        if (empty($user)) {
            throw new moodle_exception('wrongidentification');
        }
        else {
            /// TODO: probably change this
            $USER = $user;
        }
    }

/// load the external class
    require_once($CFG->dirroot.$apipath.'external.php');
    $wsapi = new $classname();
    $description = $wsapi->get_function_webservice_description($functionname); //retrieve the web service description for this function

/// This following line is only REST protocol
    $params = retrieve_params ($description); //retrieve the REST params

/// Generic part to any protocols
    if ($params === false) {
        //return an error message, the REST params doesn't match with the web service description
    }

    try {
        $res = call_user_func_array  ( $classname.'::'.$functionname, array($params));
    } catch (moodle_exception $e) {
        return "<Result>".$e->getMessage()."</Result>";
    }

///Transform result into xml in order to send the REST response
    $return =  mdl_conn_rest_object_to_xml ($res,key($description['return']));

	return "<Result>$return</Result>";
}

/**
 * TODO: remove/rewrite this funcion
 * Mock function waiting for token system implementation
 * @param <type> $token
 * @return <type>
 */
function mock_check_token($token) {
    //fake test
    if ($token == 465465465468468464) {
        ///retrieve the user
        global $DB;
        $user = $DB->get_record('user', array('username'=>'wsuser', 'mnethostid'=>1));

        if (empty($user)) {
            return false;
        }

        return $user;
    } else {
        return false;
    }
}

/**
 *
 * @author Jerome Mouneyrac
 * @param <type> $description
 * @return <type>
 */
function retrieve_params ($description) {
    $params = array();
    //retrieve REST param matching the description (warning: PHP assign the first instanciation as the first position in the table)
    foreach ($description['params'] as $paramname => $paramtype) {
        $value = optional_param($paramname,null,$paramtype);
        if (!empty($value)) {
                $params[$paramname] = $value;
        }
    }
    //retrieve REST optional params
    foreach ($description['optionalparams'] as $paramname => $paramtype) {
        $value = optional_param($paramname,null,$paramtype);
        if (!empty($value)) {
                $params[$paramname] = $value;
        }
    }

    return $params;
}

/**
 * auxiliar function for simplexml_object_to_xml
 * @author Ferran Recio, David Castro Garcia
 * @param $obj
 * @param $tag
 * @param $atts assoc array (key => value)
 * @return string
 */
function mdl_conn_rest_object_to_xml ($obj, $tag,$atts=false) {
	$res = '';
	$tag_atts = '';
	if ($atts) {
		$main_atts = array();
		foreach ($atts as $att=>$val) {
			$main_atts[] = "$att=\"".urlencode($val)."\"";
		}
		if (count($main_atts)) $tag_atts = ' '.implode(' ',$main_atts);
	}

	//if is an object
	if (is_object($obj)) {
		$parts = get_object_vars($obj);
		foreach ($parts as $tag2 => $val) {
			$res.= mdl_conn_rest_object_to_xml ($val, $tag2);
		}
		return "<$tag$tag_atts>\n$res</$tag>\n";
	}
	//if it's an array all elements will be inside te same tag but with a new atribute key
	if (is_array($obj)){
		if (!$atts) $atts = array();
		//we came from another array
		if (isset($atts['keys'])) $atts = array();
		foreach ($obj as $key=>$val) {
			$array_atts = $atts;
			$array_atts['key'] = $key;
			$res.= mdl_conn_rest_object_to_xml ($val, $tag,$array_atts);
		}
		return $res;
	}
	//any other type, just encapsule it
	$obj = htmlentities($obj);
	return  "<$tag$tag_atts>$obj</$tag>\n";

}

?>