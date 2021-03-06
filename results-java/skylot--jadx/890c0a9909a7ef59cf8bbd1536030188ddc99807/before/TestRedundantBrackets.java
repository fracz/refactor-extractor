package jadx.tests.internal;

import jadx.api.InternalJadxTest;
import jadx.core.dex.nodes.ClassNode;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TestRedundantBrackets extends InternalJadxTest {

	public static class TestCls {
		public boolean method(String str) {
			return str.indexOf('a') != -1;
		}

		public int method2(Object obj) {
			if (obj instanceof String) {
				return ((String) obj).length();
			}
			return 0;
		}

		public int method3(int a, int b) {
			if (a + b < 10) {
				return a;
			}
			if ((a & b) != 0) {
				return a * b;
			}
			return b;
		}

		public void method4(int num) {
			if (num == 4 || num == 6 || num == 8 || num == 10) {
				method2(null);
			}
		}

		public void method5(int a[], int n) {
			a[1] = n * 2;
			a[n - 1] = 1;
		}
	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);
		String code = cls.getCode().toString();

		assertThat(code, not(containsString("(-1)")));
		assertThat(code, not(containsString("return;")));
		assertThat(code, either(containsString("if (obj instanceof String) {"))
				.or(containsString("return (obj instanceof String) ? ")));
		assertThat(code, containsString("if (a + b < 10)"));
		assertThat(code, containsString("if ((a & b) != 0)"));
		assertThat(code, containsString("if (num == 4 || num == 6 || num == 8 || num == 10)"));

		assertThat(code, containsString("a[1] = n * 2;"));
		assertThat(code, containsString("a[n - 1] = 1;"));

		// argument type not changed to String
		assertThat(code, containsString("public int method2(Object obj) {"));
		// cast not eliminated
		assertThat(code, containsString("((String) obj).length()"));
	}
}