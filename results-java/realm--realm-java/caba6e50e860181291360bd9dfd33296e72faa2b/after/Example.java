package com.tightdb.example;

import java.util.Date;
import java.util.List;

import com.tightdb.example.generated.Person;
import com.tightdb.example.generated.PersonTable;
import com.tightdb.example.generated.PersonView;

public class Example {

	// @NestedTable
	class phone {
		String type;
		String number;
	}

	// @Table
	class person {
		String firstName;
		String lastName;
		int salary;
		phone phones;
	}

	public static void main(String[] args) {
		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 23000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		john.phones.get().add("home", "123456");
		john.phones.get().add("mobile", "333444");

		persons.insert(0, "Nikolche", "Mihajlovski", 28000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		// 2 ways to get the value
		String name1 = persons.at(0).firstName.get();
		String name2 = persons.at(0).getFirstName();

		// 2 ways to set the value
		persons.at(1).lastName.set("NewName");
		persons.at(1).setLastName("NewName");

		persons.remove(0);

		Person johnDoe = persons.firstName.is("John").findUnique();

		PersonView allRich = persons.salary.greaterThan(100000).findAll();

		// using explicit OR
		// Person johnny = persons.firstName.is("Johnny").or().salary.is(10000).findFirst();

		// using implicit AND
		Person johnnyB = persons.firstName.is("Johnny").lastName.startsWith("B").findUnique();

		persons.firstName.is("John").findLast().salary.set(30000);

		PersonView nikolches = persons.firstName.is("Nikolche").findAll();

		// projection and aggregation of the salary
		long salarySum = persons.salary.sum();

		// lazy iteration through the table - now simpler
		for (Person person : persons) {
			person.salary.set(50000);
		}

		// using lazy list of results - as moving a cursor through a view
		PersonView view = persons.salary.greaterThan(123).findAll();
		for (Person person : view) {
			System.out.println(person);
		}
		long maxSalary = view.salary.max();

		// Various combinations:

		// option 1: direct query and data retrieval
		// int sum1 = persons.firstName.is("X").or().salary.is(5).salary.sum();

		// options 2:
		// int sum2 = persons.firstName.is("X").or().salary.is(5).findAll().salary.sum();

		persons.firstName.is("Y").salary.is(6).lastName.setAll("Z");
		persons.salary.greaterThan(1234).clear();

		for (String phone : persons.phones.get().type.is("mobile").findAll().phone.getAll()) {
			System.out.println(phone);
		}

		// from 2nd to 4th row
		view = persons.range(2, 4);

		// TODO: discuss the trade-offs (new class, longer queries or incorrect
		// options?) between such options:
		persons.firstName.is("d").salary.sum(); // no problem without OR
		persons.firstName.is("X").or().salary.set(1234); // problem - incorrect
															// options

		// persons.firstName.is("X").or().salary.is(23).findAll().salary.sum();

		Person p1 = persons.at(4).next(); // 5nd row
		Person p2 = persons.last().previous(); // 2nd-last row
		Person p3 = persons.first().after(3); // 4th row
		Person p4 = persons.last().before(2); // 3rd-last row

		// TODO: cursor navigation in views:
		PersonView allJohns = persons.firstName.is("John").findAll();
		// Person firstJohn = allJohns.first();
		// Person thirdJohn = allJohns.at(2).next();

		// TODO: discuss with Brian: row numbers after deletion and insertion
		// (IDs vs. row numbers)
		// maybe we will need (long ID or String ID) in the model

		// TODO: discuss with Brian: tables and view can be very similar and
		// have common operations:
		// - both can be lists (currently extend that behaviour from
		// AbstractRowset)
		// both can allow cursor navigation: at(), first(), last()...
		// both allow aggregation: x.salary.sum()

		// TODO: add empty row?
		// TODO: sort, limit

		// TODO: in future introduce and implement Entity and Column interfaces
		// as public API
	}

}