/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package a.org.fakereplace.test.coverage.report;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import a.org.fakereplace.test.coverage.ChangeTestType;
import a.org.fakereplace.test.coverage.CodeChangeType;
import a.org.fakereplace.test.coverage.Coverage;
import a.org.fakereplace.test.coverage.MultipleCoverage;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class CoverageListener implements ITestListener {

    static private final Map<CodeChangeType, Map<ChangeTestType, Set<String>>> publicTest = new HashMap<CodeChangeType, Map<ChangeTestType, Set<String>>>();
    static private final Map<CodeChangeType, Map<ChangeTestType, Set<String>>> privateTest = new HashMap<CodeChangeType, Map<ChangeTestType, Set<String>>>();

    public void onFinish(ITestContext context) {
        // TODO Auto-generated method stub

    }

    public void onStart(ITestContext context) {
        // TODO Auto-generated method stub

    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // TODO Auto-generated method stub

    }

    public void onTestFailure(ITestResult result) {
        // TODO Auto-generated method stub

    }

    public void onTestSkipped(ITestResult result) {
        handleAnnotations(result.getMethod().getMethod().getDeclaredAnnotations(), result.getMethod().getMethod());
    }

    public void onTestStart(ITestResult result) {
        handleAnnotations(result.getMethod().getMethod().getDeclaredAnnotations(), result.getMethod().getMethod());

    }

    public void onTestSuccess(ITestResult result) {
        // TODO Auto-generated method stub

    }

    private static void handleAnnotations(Annotation[] annotations, Method m) {
        for (Annotation a : annotations) {
            if (a instanceof Coverage) {
                handleCoverage((Coverage) a, m);
            } else if (a instanceof MultipleCoverage) {
                handleMultipleCoverage((MultipleCoverage) a, m);
            }
        }
    }

    private static void handleMultipleCoverage(MultipleCoverage a, Method m) {
        for (Coverage i : a.value()) {
            handleCoverage(i, m);
        }
    }

    private static void handleCoverage(Coverage a, Method m) {
        Map<CodeChangeType, Map<ChangeTestType, Set<String>>> map;
        if (a.privateMember()) {
            map = privateTest;
        } else {
            map = publicTest;
        }
        Map<ChangeTestType, Set<String>> res = map.get(a.change());
        if (res == null) {
            res = new HashMap<ChangeTestType, Set<String>>();
            map.put(a.change(), res);
        }
        Set<String> data = res.get(a.test());
        if (data == null) {
            data = new HashSet<String>();
            res.put(a.test(), data);
        }
        data.add(m.getDeclaringClass().getName() + "." + m.getName());
    }

    public static Map<CodeChangeType, Map<ChangeTestType, Set<String>>> getPublictest() {
        return publicTest;
    }

    public static Map<CodeChangeType, Map<ChangeTestType, Set<String>>> getPrivatetest() {
        return privateTest;
    }

}
