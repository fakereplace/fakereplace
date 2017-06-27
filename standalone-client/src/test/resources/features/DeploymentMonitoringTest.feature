Feature: Test if deployment monitoring works.

  @1
  Scenario: When changing resources, the data should be pushed
    Given I monitor the default test folder with no package restrictions
    When I change two resources, one in the monitored folder, the other outside
    Then I get one changed monitored resource

  @2
  Scenario: When changing classes with package restrictions, the classes should be pushed
    Given I monitor the 'com.monitored' package in the default test folder
    When I change a class inside the monitored folder, in the monitored package
    And I change a class inside the monitored folder, in a package that is not monitored
    And I change a class outside a monitored folder
    Then I get one changed monitored class

  @3
  Scenario: When changing classes without package restrictions, the classes should be pushed
    Given I monitor the default test folder with no package restrictions
    When I change a class inside the monitored folder, in the monitored package
    And I change a class inside the monitored folder, in a package that is not monitored
    And I change a class outside a monitored folder
    Then I get two changed monitored classes

  @4
  Scenario: When simultaneously changing classes and resources, all should be pushed
    Given I monitor the 'com.monitored' package in the default test folder
    When I change a class inside the monitored folder, in the monitored package
    And I change a class inside the monitored folder, in a package that is not monitored
    And I change a class outside a monitored folder
    And I change two resources, one in the monitored folder, the other outside
    Then I get one changed monitored resource
    And I get one changed monitored class

  @5
  Scenario: When simultaneously changing classes and resources, all should be pushed
    Given I monitor the default test folder with no package restrictions
    When I change a class inside the monitored folder, in the monitored package
    And I change a class inside the monitored folder, in a package that is not monitored
    And I change a class outside a monitored folder
    And I change two resources, one in the monitored folder, the other outside
    Then I get one changed monitored resource
    And I get two changed monitored classes
