package org.fakereplace.test.basic.replacement;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

public class BasicReplacementTest
{
	@Test
	public void testInstrumentationReplacement()
	{
		BasicTestRep re = new BasicTestRep();
		int val0 = re.value();
		assert val0 == 0 : "Test setup wrong";
		ClassReplacer cr = new ClassReplacer();
		cr.queueClassForReplacement(BasicTestRep.class, BasicTestRep1.class);
		cr.replaceQueuedClassesWithInstrumentation();
		re = new BasicTestRep();
		val0 = re.value();
		assert val0 == 1 : "BasicTestRep was not replaced";

	}

	@Test
	public void testFakeReplaceReplacement()
	{
		InstTestRep re = new InstTestRep();
		int val0 = re.value();
		assert val0 == 0 : "Test setup wrong";
		ClassReplacer cr = new ClassReplacer();
		cr.queueClassForReplacement(InstTestRep.class, InstTestRep1.class);
		cr.replaceQueuedClasses();
		re = new InstTestRep();
		val0 = re.value();
		assert val0 == 1 : "InstTestRep was not replaced";

	}

}
