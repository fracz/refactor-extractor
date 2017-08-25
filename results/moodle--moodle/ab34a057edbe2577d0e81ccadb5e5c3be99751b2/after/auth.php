<?PHP // $Id$
      // auth.php - created with Moodle 1.0.8.1 (2003011200)


$string['auth_dbdescription'] = "T�to met�da vyu��va extern� datab�zov� tabu�ku na kontrolu platnosti dan�ho u��vate�sk�ho mena a hesla. Ak je to nov� konto, m��u by� do Moodle prenesen� inform�cie aj z in�cho pol��ok.";
$string['auth_dbextrafields'] = "Tieto pol��ka s� nepovinn�. Je tu mo�nos�, aby niektor� u��vate�sk� pol��ka syst�mu uv�dzali inform�cie z <B>pol��ok extern�ch datab�z</B> ,ktor� tu ud�te. <P>Ak tu ni� neuvediete, bude uv�dzan� p�vodn� nastavenie.<P>V obidvoch pr�padoch bude m�c� u��vate� po prihl�sen� korigova� v�etky tieto pol��ka.";
$string['auth_dbfieldpass'] = "N�zov pol��ka obsahuje hesl�";
$string['auth_dbfielduser'] = "N�zov pol��ka obsahuje u��vate�sk� men�";
$string['auth_dbhost'] = "Po��ta� hos�uj�ci datab�zov� server";
$string['auth_dbname'] = "Vlastn� n�zov datab�zy";
$string['auth_dbpass'] = "Heslo je identick� s uveden�m u��vate�om";
$string['auth_dbpasstype'] = "�pecifkujte form�t, ktor� pou��va pol��ko pre heslo. MD5 �ifrovanie je vhodn� pre pripojenie k �al��m be�n�m web aplik�ci�m ako PostNuke.";
$string['auth_dbtable'] = "N�zov tabu�ky je v datab�ze";
$string['auth_dbtitle'] = "Pou�i� extern� datab�zu";
$string['auth_dbtype'] = "Datab�zov� typ (bli��ie vi�<A HREF=../lib/adodb/readme.htm#drivers>ADOdb documentation</A> )";
$string['auth_dbuser'] = "U��vate�sk� meno s pr�stupom do datab�zy len na ��tanie.";
$string['auth_emaildescription'] = "Sp�sob overovania je nastaven� ako potvrdzovanie prostredn�ctvom emailu. Ke� sa u��vate� prihl�si, vyberie si vlastn� nov� u��vate�sk� meno a heslo a dostane potvrdzovac� email na svoju emailov� adresu. Tento email obsahuje bezpe�nostn� linku na str�nku, kde m��e u��vate� potvrdi� svoje nastavenie. Pri �al��ch prihlasovaniach iba skontroluje u��vate�sk� meno a heslo v porovnan� s �dajmi ulo�en�mi v datab�ze syst�mu.";
$string['auth_emailtitle'] = "Overovanie emailom";
$string['auth_imapdescription'] = "Na kontrolu spr�vnosti dan�ho u��vate�sk�ho mena a hesla pou��va t�to met�da IMAP server.";
$string['auth_imaphost'] = "Adresa IMAP serveru. Pou��vajte ��slo IP, nie n�zov DNS.";
$string['auth_imapport'] = "��slo IMAP server portu. Zvy�ajne je to 143 alebo 993.";
$string['auth_imaptitle'] = "IMAP server";
$string['auth_imaptype'] = "Typ IMAP serveru. IMAP servery m��u ma� rozli�n� typy overovania.";
$string['auth_ldap_bind_dn'] = "Ak chcete pou��va� spoluu��vate�ov, aby ste mohli h�ada� u��vate�ov uve�te to tu. Napr�klad: 'ou=users,o=org; ou=others,o=org'";
$string['auth_ldap_bind_pw'] = "Heslo pre spoluu��vate�ov.";
$string['auth_ldap_contexts'] = "Zoznam prostred�, kde sa nach�dzaj� u��vatelia. Odde�te rozli�n� prostredia s ';'. Napr�klad: 'ou=users,o=org; ou=others,o=org'";
$string['auth_ldap_host_url'] = "�pecifikujte hostite�a LDAP vo forme URL tj. 'ldap://ldap.myorg.com/' alebo 'ldaps://ldap.myorg.com/' ";
$string['auth_ldap_search_sub'] = "Uve�te hodnotu &lt;&gt; 0 ak chcete h�ada� u��vate�ov v subkontextoch.";
$string['auth_ldap_update_userinfo'] = "Aktualizova� inform�cie o u��vate�ovi (krstn� meno, priezvisko, adresa,...) z LDAP do syst�mu. H�ada� v /auth/ldap/attr_mappings.php pre prira�uj�ce inform�cie.";
$string['auth_ldap_user_attribute'] = "Vlastnos� pou��van� na h�adanie mien u��vate�ov. Zvy�ajne 'cn'.";
$string['auth_ldapdescription'] = "T�to met�da poskytuje overovanie s LDAP serverom.
                                  Ak je u��vate�sk� meno a heslo spr�vne, syst�m vytvor� nov� vstup u��vate�a do jeho datab�zy.
								  Tento modul dok�e ��ta� u��vate�sk� vlastnosti z LDAP a vyplni� �elan� pol��ka v syst�me.
								Pre nasleduj�ce prihlasovania sa kontroluj� iba u��vate�sk� meno a heslo.";
$string['auth_ldapextrafields'] = "Tieto pol��ka s� nepovinn�. Je tak� mo�nos�, �e u��vate�sk� pol��ka syst�mu bud� uv�dza� inform�cie z <B>LDAP pol��ok</B> ,ktor� tu ud�te. <P>Ak tu ni� neuvediete, inform�cie z LDAP nebud� preveden�, a namiesto toho bude uv�dzan� Moodle nastavenie. <P>V obidvoch pr�padoch bude m�c� u��vate� po prihl�sen� korigova� v�etky tieto pol��ka.";
$string['auth_ldaptitle'] = "LDAP server";
$string['auth_nntpdescription'] = "Tento postup pou��va na kontrolu spr�vnosti u��vate�sk�ho mena a hesla NNTP server.";
$string['auth_nntphost'] = "Adresa NNTP servera. Pou�ite ��slo IP, nie n�zov DNS.";
$string['auth_nntpport'] = "Port serveru (119 je najbe�nej��)";
$string['auth_nntptitle'] = "NNTP server";
$string['auth_nonedescription'] = "U��vatelia sa m��u prihl�si� a vytvori� kont� bez overovania s extern�m serverom a bez potvrdzovania prostredn�ctvom emailu. Pri tejto vo�be bu�te opatrn� - myslite na bezpe�nos� a probl�my pri administr�cii, ktor� t�m  m��u vznikn��.";
$string['auth_nonetitle'] = "�iadne overenie";
$string['auth_pop3description'] = "Tento postup pou��va  na kontrolu spr�vnosti u��vate�sk�ho mena a hesla POP3 server.";
$string['auth_pop3host'] = "Adresa POP3 servera. Pou�ite ��slo IP , nie n�zov DNS.";
$string['auth_pop3port'] = "Server port (110 je najbe�nej��)";
$string['auth_pop3title'] = "POP3 server";
$string['auth_pop3type'] = "Typ servera. Ak v� server pou��va certifikovan� zabezpe�enie, vyberte si pop3cert.";
$string['authenticationoptions'] = "Mo�nosti overovania";
$string['authinstructions'] = "Tu m��ete uvies� pokyny pre u��vate�ov, aby vedeli, ak� u��vate�sk� meno a heslo maj� pou��va�. Text, ktor� tu vlo��te sa objav� na prihlasovacej str�nke. Ak to tu neuvediete, nebud� zobrazen� �iadne pokyny.";
$string['changepassword'] = "Zmeni� heslo URL";
$string['changepasswordhelp'] = "Tu m��ete uvies� miesto, na ktorom si va�i u��vatelia m��u pripomen��, alebo zmeni� u��vate�sk� meno/heslo, ak ho zabudli. Pre u��vate�ov to bude zobrazen� ako tla�idlo na prihlasovacej str�nke ich u��vate�skej str�nky. Ak to tu neuvediete, tla�idlo sa nezobraz�.";
$string['chooseauthmethod'] = "Vyberte si met�du overenia : ";
$string['guestloginbutton'] = "Prihlasovacie tla�idlo pre hos�a";
$string['instructions'] = "In�trukcie";
$string['md5'] = "MD5 �ifrovanie";
$string['plaintext'] = "�ist� text";
$string['showguestlogin'] = "M��ete skry�, alebo zobrazi� prihlasovacie tla�idlo pre hos�a na prihlasovacej str�nke.";

?>