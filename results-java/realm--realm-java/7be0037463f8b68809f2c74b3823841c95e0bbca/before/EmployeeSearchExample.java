package com.tightdb.example;

import java.util.Arrays;
import java.util.Date;

import com.tightdb.generated.Employee;
import com.tightdb.generated.EmployeeQuery;
import com.tightdb.generated.EmployeeTable;
import com.tightdb.generated.EmployeeView;
import com.tightdb.lib.TDBUtils;

public class EmployeeSearchExample {

	public static void main(String[] args) {
		EmployeeTable employees = new EmployeeTable();

		Employee john = employees.add("John", "Doe", 10000, true, new byte[] { 1, 2, 3 }, new Date(), "extra");
		Employee johny = employees.add("Johny", "Goe", 20000, true, new byte[] { 1, 2, 3 }, new Date(), true);
		Employee nikolche = employees.insert(1, "Nikolche", "Mihajlovski", 30000, false, new byte[] { 4, 5 }, new Date(), 1234.56);

		TDBUtils.print(employees);

		System.out.println("Query 1:");
		TDBUtils.print(employees.firstName.startsWith("Nik").lastName.contains("vski").or().firstName.is("John").findAll());

		System.out.println("Query 2a:");
		TDBUtils.print(employees.firstName.startsWith("Nik").startGroup().lastName.contains("vski").or().firstName.is("John").endGroup().findAll());

		System.out.println("Query 2b:");
		TDBUtils.print(employees.query().startGroup().lastName.contains("vski").or().firstName.is("John").endGroup().firstName.startsWith("Nik").findAll());

		EmployeeQuery q1 = employees.firstName.startsWith("J").lastName.endWith("e");
		System.out.println(q1);

		EmployeeView results = q1.findAll();
		System.out.println(results);

		Employee first = q1.findFirst();
		Employee last = q1.findLast();
		System.out.println("First result: " + first);
		System.out.println("Last result: " + last);

		Employee niko = employees.firstName.startsWith("Nik").findUnique();
		System.out.println("Unique Niko: " + niko);

		TDBUtils.print(results);

		System.out.println("First names: " + Arrays.toString(results.firstName.getAll()));
		System.out.println("Salary sum: " + results.salary.sum());
		System.out.println("Salary min: " + results.salary.min());
		System.out.println("Salary max: " + results.salary.max());

		TDBUtils.print(results);

		results.clear();

		TDBUtils.print(employees);

		long count = employees.firstName.contains("iko").clear();
		System.out.println("Removed " + count + " rows!");

		TDBUtils.print(employees);

		try {
			results.salary.greaterThan(3);
		} catch (IllegalStateException e) {
			System.out.println(e.getMessage());
		}

	}

}