commit 5704585cf4876302ad21093b1ff1aca3c63d1fbb
Author: Inaki <iarenuno@eteo.mondragon.edu>
Date:   Mon Jul 19 21:52:52 2010 +0000

    enrol/ldap: MDL-22784 Conversion of the LDAP enrolment plugin to the new enrolment infrastructure.

    We've moved some of the LDAP auth plugin "knowledge" of different LDAP
    servers to a common library, and we've added support for nested groups
    enrolments for selected LDAP servers.

    Lots of changes in the language pack, as all the plugin messages have been
    internationali[sz]ed (it also includes renaming of quite a few string
    identifiers).

    During plugin settings migration, we rename 'version' setting to
    'ldap_version', as we store the plugin version itself as a setting in the
    plugin config table and the two collided.

    Performance is not great for larga data sets but this can be improved later
    (20000 users, 4000 courses and 100 users/course take around 1h25m on a somewhat
    tuned postgresql-8.4 on a 2.66GHz Core2 Duo w/ 4 GB of RAM).

    AMOS BEGIN
      MOV [description,enrol_ldap],[pluginname_desc,enrol_ldap]
      MOV [enrol_ldap_autocreate,enrol_ldap],[autocreate,enrol_ldap]
      MOV [enrol_ldap_autocreate_key,enrol_ldap],[autocreate_key,enrol_ldap]
      MOV [enrol_ldap_autocreation_settings,enrol_ldap],[autocreation_settings,enrol_ldap]
      MOV [enrol_ldap_bind_dn,enrol_ldap],[bind_dn,enrol_ldap]
      MOV [enrol_ldap_bind_dn_key,enrol_ldap],[bind_dn_key,enrol_ldap]
      MOV [enrol_ldap_bind_pw,enrol_ldap],[bind_pw,enrol_ldap]
      MOV [enrol_ldap_bind_pw_key,enrol_ldap],[bind_pw_key,enrol_ldap]
      MOV [enrol_ldap_bind_settings,enrol_ldap],[bind_settings,enrol_ldap]
      MOV [enrol_ldap_category,enrol_ldap],[category,enrol_ldap]
      MOV [enrol_ldap_category_key,enrol_ldap],[category_key,enrol_ldap]
      MOV [enrol_ldap_contexts,enrol_ldap],[contexts,enrol_ldap]
      MOV [enrol_ldap_course_fullname,enrol_ldap],[course_fullname,enrol_ldap]
      MOV [enrol_ldap_course_fullname_key,enrol_ldap],[course_fullname_key,enrol_ldap]
      MOV [enrol_ldap_course_idnumber,enrol_ldap],[course_idnumber,enrol_ldap]
      MOV [enrol_ldap_course_idnumber_key,enrol_ldap],[course_idnumber_key,enrol_ldap]
      MOV [enrol_ldap_course_search_sub,enrol_ldap],[course_search_sub,enrol_ldap]
      MOV [enrol_ldap_course_settings,enrol_ldap],[course_settings,enrol_ldap]
      MOV [enrol_ldap_course_shortname,enrol_ldap],[course_shortname,enrol_ldap]
      MOV [enrol_ldap_course_shortname_key,enrol_ldap],[course_shortname_key,enrol_ldap]
      MOV [enrol_ldap_course_summary,enrol_ldap],[course_summary,enrol_ldap]
      MOV [enrol_ldap_course_summary_key,enrol_ldap],[course_summary_key,enrol_ldap]
      MOV [enrol_ldap_editlock,enrol_ldap],[editlock,enrol_ldap]
      MOV [enrol_ldap_ldap_encoding,enrol_ldap],[ldap_encoding,enrol_ldap]
      MOV [enrol_ldap_ldap_encoding_key,enrol_ldap],[ldap_encoding_key,enrol_ldap]
      MOV [enrol_ldap_general_options,enrol_ldap],[general_options,enrol_ldap]
      MOV [enrol_ldap_group_memberofattribute,enrol_ldap],[group_memberofattribute,enrol_ldap]
      MOV [enrol_ldap_group_memberofattribute_key,enrol_ldap],[group_memberofattribute_key,enrol_ldap]
      MOV [enrol_ldap_host_url,enrol_ldap],[host_url,enrol_ldap]
      MOV [enrol_ldap_host_url_key,enrol_ldap],[host_url_key,enrol_ldap]
      MOV [enrol_ldap_idnumber_attribute,enrol_ldap],[idnumber_attribute,enrol_ldap]
      MOV [enrol_ldap_idnumber_attribute_key,enrol_ldap],[idnumber_attribute_key,enrol_ldap]
      MOV [enrol_ldap_memberattribute,enrol_ldap],[memberattribute,enrol_ldap]
      MOV [enrol_ldap_memberattribute_isdn,enrol_ldap],[memberattribute_isdn,enrol_ldap]
      MOV [enrol_ldap_memberattribute_isdn_key,enrol_ldap],[memberattribute_isdn_key,enrol_ldap]
      MOV [enrol_ldap_nested_groups,enrol_ldap],[nested_groups,enrol_ldap]
      MOV [enrol_ldap_nested_groups_key,enrol_ldap],[nested_groups_key,enrol_ldap]
      MOV [enrol_ldap_nested_groups_settings,enrol_ldap],[nested_groups_settings,enrol_ldap]
      MOV [enrol_ldap_objectclass,enrol_ldap],[objectclass,enrol_ldap]
      MOV [enrol_ldap_objectclass_key,enrol_ldap],[objectclass_key,enrol_ldap]
      MOV [enrol_ldap_opt_deref,enrol_ldap],[opt_deref,enrol_ldap]
      MOV [enrol_ldap_opt_deref_key,enrol_ldap],[opt_deref_key,enrol_ldap]
      MOV [enrol_ldap_roles,enrol_ldap],[roles,enrol_ldap]
      MOV [enrol_ldap_search_sub_key,enrol_ldap],[search_sub_key,enrol_ldap]
      MOV [enrol_ldap_server_settings,enrol_ldap],[server_settings,enrol_ldap]
      MOV [enrol_ldap_template,enrol_ldap],[template,enrol_ldap]
      MOV [enrol_ldap_template_key,enrol_ldap],[template_key,enrol_ldap]
      MOV [enrol_ldap_updatelocal,enrol_ldap],[updatelocal,enrol_ldap]
      MOV [enrol_ldap_user_attribute,enrol_ldap],[user_attribute,enrol_ldap]
      MOV [enrol_ldap_user_attribute_key,enrol_ldap],[user_attribute_key,enrol_ldap]
      MOV [enrol_ldap_user_contexts,enrol_ldap],[user_contexts,enrol_ldap]
      MOV [enrol_ldap_user_contexts_key,enrol_ldap],[user_contexts_key,enrol_ldap]
      MOV [enrol_ldap_user_search_sub,enrol_ldap],[user_search_sub,enrol_ldap]
      MOV [enrol_ldap_user_settings,enrol_ldap],[user_settings,enrol_ldap]
      MOV [enrol_ldap_user_type,enrol_ldap],[user_type,enrol_ldap]
      MOV [enrol_ldap_user_type_key,enrol_ldap],[user_type_key,enrol_ldap]
      MOV [enrol_ldap_version,enrol_ldap],[version,enrol_ldap]
      MOV [enrol_ldap_version_key,enrol_ldap],[version_key,enrol_ldap]
      MOV [search_sub_key,enrol_ldap],[course_search_sub_key,enrol_ldap]
    AMOS END