package org.fakereplace.test.coverage;

public enum CodeChangeType
{
   ADD_STATIC_METHOD("Add static method"),
   REMOVE_STATIC_METHOD("Remove static method"),
   EXISTING_STATIC_METHOD("Existing static method"),
   ADD_INSTANCE_METHOD("Add instance method"),
   REMOVE_INSTANCE_METHOD("Remove instance method"),
   EXISTING_INSTANCE_METHOD("Existing instance method"),
   ADD_CONSTRUCTOR("Add Constructor"),
   REMOVE_CONSTRUCTOR("Remove Constructor"),
   EXISTING_CONSTRUCTOR("Existing Constructor"),
   ADD_STATIC_FIELD("Add static field"),
   REMOVE_STATIC_FIELD("Remove static field"),
   EXISTING_STATIC_FIELD("Existing static field"),
   ADD_INSTANCE_FIELD("Add instance field"),
   REMOVE_INSTANCE_FIELD("Remove instance field"),
   EXISTING_INSTANCE_FIELD("Existing instance field"),
   STATIC_FIELD_TO_INSTANCE("Static field to instance field"),
   INSTANCE_FIELD_TO_STATIC_FIELD("Instance field to static field");

   private final String label;

   private CodeChangeType(String label)
   {
      this.label = label;
   }

   public String getLabel()
   {
      return label;
   }

}
