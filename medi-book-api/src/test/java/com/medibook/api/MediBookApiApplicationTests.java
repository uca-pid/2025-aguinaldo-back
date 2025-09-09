package com.medibook.api;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.springframework.boot.test.context.SpringBootTest;

@Suite
@SpringBootTest
@SelectPackages("com.medibook.api")
@SuiteDisplayName("MediBook API Test Suite")
class MediBookApiApplicationTests {
    // Test suite configuration class
    // Will automatically discover and run all tests in com.medibook.api package
}