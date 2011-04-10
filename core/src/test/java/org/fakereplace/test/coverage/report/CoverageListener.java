package org.fakereplace.test.coverage.report;

import org.fakereplace.test.coverage.ChangeTestType;
import org.fakereplace.test.coverage.CodeChangeType;
import org.fakereplace.test.coverage.Coverage;
import org.fakereplace.test.coverage.MultipleCoverage;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
