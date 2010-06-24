package org.fakereplace.test.coverage;

public enum ChangeTestType
{
   GET_BY_NAME("Get by name"),
   GET_DECLARED_BY_NAME("Get declared by name"),
   GET_DELCARED_ALL("Get declared all"),
   GET_ALL("Get all"),
   INVOKE_BY_REFLECTION("Invoke by reflection", false),
   ACCESS_THROUGH_BYTECODE("Access through bytecode", false);

   private final String label;
   private final boolean applicableToRemoved;

   private ChangeTestType(String label, boolean applicableToRemoved)
   {
      this.label = label;
      this.applicableToRemoved = applicableToRemoved;
   }

   private ChangeTestType(String label)
   {
      this.label = label;
      this.applicableToRemoved = true;
   }

   public String getLabel()
   {
      return label;
   }

   public boolean isApplicableToRemoved()
   {
      return applicableToRemoved;
   }

}
