package org.fakereplace.test.basic.replacement.method;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Test that tests simple and not-so simple aspects of method replacement
 *
 * @author Stuart Douglas
 */
public class MethodReplacementTest {

    @BeforeGroups(value = {"firstReplacement"})
    public void setup() {

    }

    @Test(groups = {"firstReplacement"})
    public void test1() {

    }

    @BeforeGroups(value = {"secondReplacement"}, dependsOnGroups = {"firstReplacement"})
    public void setupSecondTest() {

    }

    @Test(groups = {"secondReplacement"})
    public void test2() {

    }

}
