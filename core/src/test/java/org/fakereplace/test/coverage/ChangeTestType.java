package org.fakereplace.test.coverage;

public enum ChangeTestType
{
   GET_BY_NAME("Get by name"),
   GET_DECLARED_BY_NAME("Get declared by name"),
   GET_DELCARED_ALL("Get declared all"),
   GET_ALL("Get all"),
   INVOKE_BY_REFLECTION("Invoke by reflection"),
   ACCESS_THROUGH_BYTECODE("Access through bytecode");

   private final String label;

   private ChangeTestType(String label)
   {
      this.label = label;
   }

   public String getLabel()
   {
      return label;
   }

}
