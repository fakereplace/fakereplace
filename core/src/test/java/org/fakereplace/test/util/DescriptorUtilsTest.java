package org.fakereplace.test.util;

import org.fakereplace.util.DescriptorUtils;
import org.testng.annotations.Test;

public class DescriptorUtilsTest {

	@Test
	public void testMethodParsing() throws ClassNotFoundException {
		Class[] test = DescriptorUtils.argumentStringToClassArray("(IJ)V", this
				.getClass());
		assert test[0] == int.class;
		assert test[1] == long.class;
		test = DescriptorUtils.argumentStringToClassArray("([I[[J)V", this
				.getClass());
		assert test[0] == int[].class;
		assert test[1] == long[][].class;
		test = DescriptorUtils.argumentStringToClassArray(
				"([I[[JLjava/lang/String;)V", this.getClass());
		assert test[0] == int[].class;
		assert test[1] == long[][].class;
		assert test[2] == String.class;
		test = DescriptorUtils.argumentStringToClassArray(
				"([I[[J[Ljava/lang/String;[[[Ljava/lang/String;)V", this
						.getClass());
		assert test[0] == int[].class;
		assert test[1] == long[][].class;
		assert test[2] == String[].class;
		assert test[3] == String[][][].class;
	}

	@Test
	public void getReturnTypeTest() {
		String ret;
		ret = DescriptorUtils.getReturnType("()C");
		assert ret.equals("C") : ret;
		ret = DescriptorUtils.getReturnType("(IJ)Ljava/lang/String;");
		assert ret.equals("Ljava/lang/String;");
	}

	@Test
	public void getArgumentTest() {
		String ret;
		ret = DescriptorUtils.getArgumentString("()C");
		assert ret.equals("()") : ret;

		ret = DescriptorUtils.getArgumentString("(IJ)Ljava/lang/String;");
		assert ret.equals("(IJ)");
	}
}
