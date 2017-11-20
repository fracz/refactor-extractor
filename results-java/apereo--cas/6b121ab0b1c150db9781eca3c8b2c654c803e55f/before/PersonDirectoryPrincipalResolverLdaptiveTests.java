package org.jasig.cas.authentication.principal;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.services.persondir.support.ldap.LdaptivePersonAttributeDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.LdapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * This is {@link PersonDirectoryPrincipalResolverLdaptiveTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml", "/resolver-context.xml"})
public class PersonDirectoryPrincipalResolverLdaptiveTests extends AbstractLdapTests {

    @Autowired
    private LdaptivePersonAttributeDao attributeDao;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyResolver() {
        for (final LdapEntry entry : this.getEntries()) {
            final String username = getUsername(entry);
            final String psw = entry.getAttribute("userPassword").getStringValue();
            final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
            resolver.setAttributeRepository(this.attributeDao);
            final Principal p = resolver.resolve(new UsernamePasswordCredential(username, psw));
            assertNotNull(p);
            assertTrue(p.getAttributes().containsKey("displayName"));
        }

    }

}