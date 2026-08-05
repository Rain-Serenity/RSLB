package com.rserene.chosen.server.api.internal.util.reflect;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EnumAccessor {
   private final Class<?> enumClass;

   public Enum<?>[] getValues() {
      return (Enum<?>[])this.enumClass.getEnumConstants();
   }

   public Enum<?> indexOf(int index) {
      return this.getValues()[index];
   }

   public Enum<?> findByName(String name) throws NoSuchEnumException {
      for (Enum<?> value : this.getValues()) {
         if (value.name().equals(name)) {
            return value;
         }
      }

      throw new NoSuchEnumException(String.format("%s -> %s", this.enumClass.getName(), name));
   }

   @Generated
   public EnumAccessor(Class<?> enumClass) {
      this.enumClass = enumClass;
   }

   @Generated
   public Class<?> getEnumClass() {
      return this.enumClass;
   }
}
