package com.tightdb.lib;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import com.tightdb.example.generated.Employee;
import com.tightdb.example.generated.Phone;
import com.tightdb.example.generated.PhoneQuery;
import com.tightdb.example.generated.PhoneTable;
import com.tightdb.example.generated.PhoneView;

public class SubtableTest extends AbstractTableTest {

	@Test
	public void shouldSaveSubtableChanges() {
		Employee employee = employees.at(0);

		// check the basic operations
		PhoneTable phones1 = employee.getPhones();
		phones1.add("mobile", "123");

		assertEquals(1, phones1.size());

		PhoneTable phones2 = employee.getPhones();
		assertEquals(1, phones2.size());

		phones2.add("mobile", "456");
		assertEquals(2, phones2.size());

		phones2.insert(1, "home", "789");
		assertEquals(3, phones2.size());

		PhoneTable phones3 = employee.getPhones();
		assertEquals(2, phones3.type.eq("mobile").count());
		assertEquals(1, phones3.type.eq("home").count());

		assertEquals(1, phones3.number.eq("123").count());
		assertEquals(1, phones3.number.eq("456").count());
		assertEquals(0, phones3.number.eq("09090").count());

		// check the search operations
		PhoneQuery phoneQuery = phones3.where().number.eq("123").number.neq("wrong").type.eq("mobile").type.neq("wrong");
		assertEquals(1, phoneQuery.count());

		PhoneView all = phoneQuery.findAll();
		assertEquals(1, all.size());
		checkPhone(all.at(0), "mobile", "123");

		checkPhone(phoneQuery.findFirst(), "mobile", "123");
		checkPhone(phoneQuery.findLast(), "mobile", "123");
		checkPhone(phoneQuery.findNext(), "mobile", "123");
		assertEquals(null, phoneQuery.findNext());

		// make sure the other sub-tables and independent and were not changed
		assertEquals(0, employees.at(1).getPhones().size());
		assertEquals(0, employees.at(2).getPhones().size());

		// check the clear operation on the query
		phoneQuery.clear();
		assertEquals(2, phones1.size());

		// check the clear operation
		phones3.clear();
		assertEquals(0, phones1.size());
		assertEquals(0, phones2.size());
		assertEquals(0, phones3.size());

		employees.clear();
	}

	private void checkPhone(Phone phone, String type, String number) {
		assertEquals(type, phone.getType());
		assertEquals(number, phone.getNumber());
		assertEquals(type, phone.type.get());
		assertEquals(number, phone.number.get());
	}

}