package com.tigthdb.example;

import java.util.List;

import com.tigthdb.example.generated.Person;
import com.tigthdb.example.generated.PersonTable;
import com.tigthdb.example.generated.PersonView;
import com.tigthdb.lib.Table;

public class Example {

	public static void main(String[] args) {

		@Table
		class phoneTable {
			String type;
			String number;
		}

		@Table
		class personTable {
			String firstName;
			String lastName;
			int salary;
			phoneTable phones;
		}

		PersonTable persons = new PersonTable();

		Person john = persons.add("John", "Doe", 23000);
		john.phones.add("home", "123456");
		john.phones.add("mobile", "333444");

		persons.insert(0, "Nikolche", "Mihajlovski", 28000);

		// 2 ways to get the value
		String name1 = persons.at(0).firstName.get();
		String name2 = persons.at(0).getFirstName();

		// 2 ways to set the value
		persons.at(1).lastName.set("NewName");
		persons.at(1).setLastName("NewName");

		persons.remove(0);

		Person johnDoe = persons.firstName.is("John").findUnique();

		List<Person> allRich = persons.salary.greaterThan(100000).findAll();

		// using explicit OR
		Person johnny = persons.firstName.is("Johnny").or().salary.is(10000).findFirst();

		// using implicit AND
		Person johnnyB = persons.firstName.is("Johnny").lastName.startsWith("B").findUnique();

		persons.firstName.is("John").findLast().salary.set(30000);

		List<Person> nikolches = persons.firstName.is("Nikolche").findAll();

		// projection and aggregation of the salary
		int salarySum = persons.salary.sum();

		// lazy iteration through the table - now simpler
		for (Person person : persons) {
			person.salary.set(50000);
		}

		// using lazy list of results - as moving a cursor through a view
		PersonView view = persons.salary.greaterThan(123).findAll();
		for (Person person : view) {
			System.out.println(person);
		}
		int maxSalary = view.salary.max();

		// Various combinations:

		// option 1: direct query and data retrieval
		int sum1 = persons.firstName.is("X").or().salary.is(5).salary.sum();

		// options 2:
		int sum2 = persons.firstName.is("X").or().salary.is(5).findAll().salary.sum();

		persons.firstName.is("Y").salary.is(6).lastName.set("Z");
		persons.salary.greaterThan(1234).remove();

		for (String phone : persons.phones.type.is("mobile").findAll().phone.all()) {
			System.out.println(phone);
		}

		// FIXME: introduce new class for "or()" to disable such options:
		persons.firstName.is("X").or().salary.all();
		persons.firstName.is("X").or().salary.set(1234);
	}

}