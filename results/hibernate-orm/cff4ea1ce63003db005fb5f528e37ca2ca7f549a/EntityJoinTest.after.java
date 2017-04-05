/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.hql;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.NaturalId;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole, Jan Martiska
 */
public class EntityJoinTest extends BaseNonConfigCoreFunctionalTestCase {
    @Override
    protected Class[] getAnnotatedClasses() {
        return new Class[] {FinancialRecord.class, User.class, Customer.class};
    }

    @Before
    public void prepare() {
        createTestData();
    }

    @After
    public void cleanup() {
        deleteTestData();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInnerEntityJoins() {
        Session session = openSession();
        session.beginTransaction();

        try {
            // this should get financial records which have a lastUpdateBy user set
            List<Object[]> result = session.createQuery(
                    "select r.id, c.name, u.id, u.username " +
                            "from FinancialRecord r " +
                            "   inner join r.customer c " +
                            "	inner join User u on r.lastUpdateBy = u.username"
            ).list();

            assertThat(result.size(), is(1));
            Object[] steveAndAcme = result.get(0);
            assertThat(steveAndAcme[0], is(1));
            assertThat(steveAndAcme[1], is("Acme"));
            assertThat(steveAndAcme[3], is("steve"));

            // NOTE that this leads to not really valid SQL, although some databases might support it /
//			result = session.createQuery(
//					"select r.id, r.customer.name, u.id, u.username " +
//							"from FinancialRecord r " +
//							"	inner join User u on r.lastUpdateBy = u.username"
//			).list();
//			assertThat( result.size(), is( 1 ) );

        } finally {
            session.getTransaction().commit();
            session.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLeftOuterEntityJoins() {
        Session session = openSession();
        session.beginTransaction();

        try {
            // this should get all financial records even if their lastUpdateBy user is null
            List<Object[]> result = session.createQuery(
                    "select r.id, u.id, u.username " +
                            "from FinancialRecord r " +
                            "	left join User u on r.lastUpdateBy = u.username" +
                            "   order by r.id"
            ).list();
            assertThat(result.size(), is(2));

            Object[] stevesRecord = result.get(0);
            assertThat(stevesRecord[0], is(1));
            assertThat(stevesRecord[2], is("steve"));

            Object[] noOnesRecord = result.get(1);
            assertThat(noOnesRecord[0], is(2));
            assertNull(noOnesRecord[2]);

        } finally {
            session.getTransaction().commit();
            session.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRightOuterEntityJoins() {
        Session session = openSession();
        session.beginTransaction();

        try {
            // this should get all users even if they have no financial records
            List<Object[]> result = session.createQuery(
                    "select r.id, u.id, u.username " +
                            "from FinancialRecord r " +
                            "	right join User u on r.lastUpdateBy = u.username" +
                            "   order by u.id"
            ).list();

            assertThat(result.size(), is(2));

            Object[] steveAndAcme = result.get(0);
            assertThat(steveAndAcme[0], is(1));
            assertThat(steveAndAcme[2], is("steve"));

            Object[] janeAndNull = result.get(1);
            assertNull(janeAndNull[0]);
            assertThat(janeAndNull[2], is("jane"));
        } finally {
            session.getTransaction().commit();
            session.close();
        }
    }

    private void createTestData() {
        Session session = openSession();
        session.getTransaction().begin();

        session.save(new User(1, "steve"));
        session.save(new User(2, "jane"));
        final Customer customer = new Customer(1, "Acme");
        session.save(customer);
        session.save(new FinancialRecord(1, customer, "steve"));
        session.save(new FinancialRecord(2, customer, null));

        session.getTransaction().commit();
        session.close();
    }

    private void deleteTestData() {
        Session session = openSession();
        session.getTransaction().begin();

        session.createQuery("delete FinancialRecord").executeUpdate();
        session.createQuery("delete Customer").executeUpdate();
        session.createQuery("delete User").executeUpdate();

        session.getTransaction().commit();
        session.close();
    }

    @Entity(name = "Customer")
    @Table(name = "customer")
    public static class Customer {
        private Integer id;
        private String name;

        public Customer() {
        }

        public Customer(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity(name = "FinancialRecord")
    @Table(name = "financial_record")
    public static class FinancialRecord {
        private Integer id;
        private Customer customer;
        private String lastUpdateBy;

        public FinancialRecord() {
        }

        public FinancialRecord(Integer id, Customer customer, String lastUpdateBy) {
            this.id = id;
            this.customer = customer;
            this.lastUpdateBy = lastUpdateBy;
        }

        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @ManyToOne
        @JoinColumn
        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public String getLastUpdateBy() {
            return lastUpdateBy;
        }

        public void setLastUpdateBy(String lastUpdateBy) {
            this.lastUpdateBy = lastUpdateBy;
        }
    }

    @Entity(name = "User")
    @Table(name = "`user`")
    public static class User {
        private Integer id;
        private String username;

        public User() {
        }

        public User(Integer id, String username) {
            this.id = id;
            this.username = username;
        }

        @Id
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @NaturalId
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }


}