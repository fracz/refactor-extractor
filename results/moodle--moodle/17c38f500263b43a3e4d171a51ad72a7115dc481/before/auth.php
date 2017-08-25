<?PHP // $Id$
      // auth.php - created with Moodle 1.5 UNSTABLE DEVELOPMENT (2005012800)


$string['auth_dbdescription'] = 'Ez a m�dszer egy k�ls� adatb�zist�bl�t haszn�l a felhaszn�l� nev�nek �s jelszav�nak ellen�rz�s�re.  �j felhaszn�l� eset�n az egy�b mez�kben t�rolt inform�ci�k is �tm�solhat�k a Moodle-ba.';
$string['auth_dbextrafields'] = 'Ezek v�laszthat� mez�k. V�laszthatja azt is, hogy a Moodle a mez�k egy r�sz�t az itt megadott <b>k�ls� adatb�zismez�kb�l</b> el�re felt�ltse. <p>Ha a mez�ket �resen hagyja, a rendszer az alapbe�ll�t�sokat fogja haszn�lni.</p><p>Mindk�t esetben a felhaszn�l� bel�p�s ut�n v�ltoztathatja ezeket a mez�ket.</p>';
$string['auth_dbfieldpass'] = 'A jelsz�t tartalmaz� mez� neve';
$string['auth_dbfielduser'] = 'A felhaszn�l�nevet tartalmaz� mez� neve';
$string['auth_dbhost'] = 'Az adatb�zisszervert futtat� sz�m�t�g�p';
$string['auth_dbname'] = 'Az adatb�zis neve';
$string['auth_dbpass'] = 'A fenti felhaszn�l�n�vnek megfelel� jelsz�';
$string['auth_dbpasstype'] = 'Adja meg a jelsz�mez� form�tum�t. Az MD5 titkos�t�s hasznos olyan elterjedt webalkalmaz�sok eset�n, mint a PostNuke';
$string['auth_dbtable'] = 'A t�bla neve az adatb�zisban';
$string['auth_dbtitle'] = 'K�ls� adatb�zis haszn�lata';
$string['auth_dbtype'] = 'Az adatb�zis t�pus�val kapcsolatos r�szletek (l�sd a <a href=\"../lib/adodb/readme.htm#drivers\">ADOdb dokument�ci�t</a>.)';
$string['auth_dbuser'] = 'Az adatb�zishoz olvas�si joggal rendelkez� felhaszn�l�n�v';
$string['auth_emaildescription'] = 'Az e-mail visszaigazol�sa az alap�rtelmezett hiteles�t�si elj�r�s. Amikor a felhaszn�l� fel�ratkozik �s �j felhaszn�l�nevet, ill. jelsz�t v�laszt, egy visszaigazol� e-mailt kap a megadott e-mail c�mre. Az e-mail egy biztons�gos ugr�pontot tartalmaz arra az oldalra, ahol a felhaszn�l� visszaigazolhatja a fel�ratkoz�st. Ezut�n a bejelentkez�sek csak a nevet �s a jelsz�t ellen�rzik a Moodle adatb�zisa alapj�n.';
$string['auth_emailtitle'] = 'Hiteles�t�s e-mail alapj�n';
$string['auth_imapdescription'] = 'Ez az elj�r�s egy IMAP-szervert haszn�l annak ellen�rz�s�re, hogy a megadott felhaszn�l�n�v �s jelsz� �rv�nyes-e.';
$string['auth_imaphost'] = 'Az IMAP-szerver c�me. Az IP-c�met haszn�lja, ne a DNS-nevet.';
$string['auth_imapport'] = 'Az IMAP-szerver portsz�ma. Ez �ltal�ban 143 vagy 993.';
$string['auth_imaptitle'] = 'Haszn�ljon IMAP-szervert.';
$string['auth_imaptype'] = 'Az IMAP-szerver t�pusa. Az IMAP-szervereknek k�l�nb�z� t�pus� hiteles�t�se �s fel�lhiteles�t�se lehet.';
$string['auth_ldap_bind_dn'] = 'Ha bind-user-t k�v�n felhaszn�l�k keres�s�re haszn�lni, �ll�tsa be itt. Pl.:\'cn=ldapuser,ou=public,o=org\'';
$string['auth_ldap_bind_pw'] = 'A bind-user jelszava';
$string['auth_ldap_contexts'] = 'Kontextusok list�ja, melyekben a felhaszn�l�k tal�lhat�k. A k�l�nb�z� kontextusokat v�lassza el pontosvessz�vel. Pl.: \'ou=users,o=org; ou=others,o=org\'';
$string['auth_ldap_create_context'] = 'Ha enged�lyezte felhaszn�l�k l�trehoz�s�t e-maillel val� visszaigazol�ssal, adja meg itt a kontextust, amelyben a felhaszn�l�k l�trej�nnek. Ennek - biztons�gi okokb�l - k�l�nb�znie kell m�s felhaszn�l�k�t�l. Ezt nem kell hozz�adni az ldap_context v�ltoz�hoz, a szoftver automatikusan ebben a kontextusban keresi a felhaszn�l�kat.';
$string['auth_ldap_creators'] = 'Azon csoportok list�ja, melyek tagjai l�trehozhatnak kurzusokat. A csoportokat v�lassza el pontosvessz�vel egym�st�l. �ltal�ban p�ld�ul: \'cn=teachers,ou=staff,o=myorg\'';
$string['auth_ldap_host_url'] = 'LDAP-gazdag�p megad�sa URL-szer�en, pl.\'ldap://ldap.myorg.com/\' vagy \'ldaps://ldap.myorg.com/\'';
$string['auth_ldap_memberattribute'] = 'Adja meg a felhaszn�l�k adott csoporthoz tartoz�st jellemz� attrib�tum�t. Ez �ltal�ban a \'member\' [tag]';
$string['auth_ldap_search_sub'] = '�rja be az <> 0 �rt�keket, ha az alkontextusokban is keresni k�v�n felhaszn�l�t.';
$string['auth_ldap_update_userinfo'] = 'Felhaszn�l�i adatok (keresztn�v, vezet�kn�v, c�m...) friss�t�se LDAP-b�l a Moodle-ba. Az inform�ci�k szerkezet�t l�sd az /auth/ldap/attr_mappings.php �llom�nyban.';
$string['auth_ldap_user_attribute'] = 'Attrib�tum a felhaszn�l�k elnevez�s�hez/keres�s�hez. �ltal�ban \'cn\'.';
$string['auth_ldap_version'] = 'A szerver �ltal haszn�lt LDAP-protokoll verzi�ja.';
$string['auth_ldapdescription'] = 'Ez a m�dszer lehet�s�get ad a jogosults�g k�ls� LDAP-szerverrel t�rt�n� ellen�rz�s�re.
Ha a megadott n�v �s jelsz� �rv�nyes, a Moodle egy �j felhaszn�l�t hoz l�tre az adatb�zis�ban. Ez a modul k�pes kiolvasni a felhaszn�l� adatait az LDAP-b�l, �s kit�lti a k�telez� mez�ket a Moodle-ban. K�vetkez� bejelentkez�skor m�r csak a felhaszn�l�n�v �s a jelsz� ellen�rz�s�re ker�l sor.';
$string['auth_ldapextrafields'] = 'Ezek a mez�k nem k�telez�ek. N�h�ny felhaszn�l�i adatmez�t el�re kit�lthet a Moodle az itt megadott <B>LDAP-mez�k</B> adataival. <P>Ha ezeket a mez�ket �resen hagyja, semmilyen adat nem ker�l �t az LDAP-b�l �s a Moodle az alap�rtelmezett �rt�keket fogja haszn�lni.<P>Mindk�t esetben bejelentkez�s ut�n a felhaszn�l�nak lehet�s�ge lesz a mez�k �rt�keit m�dos�tani.';
$string['auth_ldaptitle'] = 'LDAP-szerver haszn�lata';
$string['auth_manualdescription'] = 'Ez a m�dszer nem teszi lehet�v� felhaszn�l�k sz�m�ra azonos�t�k l�trehoz�s�t. Minden felhaszn�l�i azonos�t�t az adminisztr�tornak kell k�zzel l�trehozni.';
$string['auth_manualtitle'] = 'Csak k�zzel l�trehozott felhaszn�l�i azonos�t�k';
$string['auth_nntpdescription'] = 'Ez a m�dszer egy NNTP-szerverrel ellen�rzi a felhaszn�l�n�v �s a jelsz� �rv�nyess�g�t.';
$string['auth_nntphost'] = 'Az NNTP-szerver c�me. Az IP-c�met haszn�lja, ne a DNS-nevet.';
$string['auth_nntpport'] = 'Szerverport (�ltal�ban a 119-es)';
$string['auth_nntptitle'] = 'NNTP-szerver haszn�lata';
$string['auth_nonedescription'] = 'A felhaszn�l�k azonnal fel�ratkozhatnak �s �rv�nyes felhaszn�l�i azonos�t�t hozhatnak l�tre, k�ls� jogosults�g-ellen�rz�s �s e-mailen t�rt�n� meger�s�t�s n�lk�l. �vatosan haszn�lja ezt a lehet�s�get - gondoljon a lehets�ges biztons�gi �s adminisztr�ci�s probl�m�kra.';
$string['auth_nonetitle'] = 'Nincs hiteles�t�s';
$string['auth_pop3description'] = 'Ez a m�dszer egy POP3-szerverrel ellen�rzi a felhaszn�l�n�v �s jelsz� �rv�nyess�g�t.';
$string['auth_pop3host'] = 'A POP3-szerver c�me. Az IP-c�met haszn�lja, ne a DNS-nevet.';
$string['auth_pop3port'] = 'Szerverport (�ltal�ban a 110-es)';
$string['auth_pop3title'] = 'POP3-szerver haszn�lata';
$string['auth_pop3type'] = 'Szervert�pus. Ha a szerver tan�s�tv�nyos biztons�gi modellt haszn�l, v�lassza a pop3cert-et.';
$string['auth_user_create'] = 'Felhaszn�l� l�trehoz�s�nak enged�lyez�se';
$string['auth_user_creation'] = '�j (n�vtelen) felhaszn�l�k is l�trehozhatnak �j felhaszn�l�i azonos�t�t a k�ls� hiteles�t�si forr�son, e-mailes meger�s�t�ssel. Ha ezt enged�lyezi, ne feledje megadni a felhaszn�l� l�trehoz�s�hoz a modul-specifikus adatokat.';
$string['auth_usernameexists'] = 'A v�lasztott felhaszn�l�n�v m�r l�tezik. V�lasszon m�sikat.';
$string['authenticationoptions'] = 'Felhaszn�l�-azonos�t�si lehet�s�gek';
$string['authinstructions'] = 'Itt t�j�koztathatja a felhaszn�l�kat arr�l, hogy milyen felhaszn�l�neveket �s jelszavakat haszn�lhatnak. Az itt megadott sz�veg megjelenik a bejelentkez� oldalon. Ha �resen hagyja, nem jelenik meg semmilyen t�j�koztat�s.';
$string['changepassword'] = 'Jelsz� cser�je';
$string['changepasswordhelp'] = 'Itt megadhat egy helyet, ahol a felhaszn�l�k visszakereshetik vagy megv�ltoztathatj�k felhaszn�l�i nev�ket/jelszavukat, ha elfelejtett�k. Ez gombk�nt jelenik meg a bejelentkez� oldalon �s az adott felhaszn�l� oldal�n. Ha �resen hagyja, nem jelenik meg ilyen gomb.';
$string['chooseauthmethod'] = 'V�lasszon azonos�t�si elj�r�st:';
$string['guestloginbutton'] = 'Vend�g bel�p�se gomb';
$string['instructions'] = 'T�j�koztat�s';
$string['md5'] = 'MD5-titkos�t�s';
$string['plaintext'] = 'Egyszer� sz�veg';
$string['showguestlogin'] = 'Megjelen�theti vagy elrejtheti a vend�gk�nt val� bel�p�sre szolg�l� gombot a bejelentkez� oldalon.';

?>