package com.rserene.chosen.server.api.profile;

import java.util.Objects;
import lombok.Generated;

public class Property {
   private String name;
   private String value;
   private String signature;

   public Property clone() {
      return new Property(this.name, this.value, this.signature);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Property property = (Property)o;
         return Objects.equals(this.name, property.name) && Objects.equals(this.value, property.value) && Objects.equals(this.signature, property.signature);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.value, this.signature);
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getValue() {
      return this.value;
   }

   @Generated
   public String getSignature() {
      return this.signature;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public void setValue(String value) {
      this.value = value;
   }

   @Generated
   public void setSignature(String signature) {
      this.signature = signature;
   }

   @Generated
   @Override
   public String toString() {
      return "Property(name=" + this.getName() + ", value=" + this.getValue() + ", signature=" + this.getSignature() + ")";
   }

   @Generated
   public Property(String name, String value, String signature) {
      this.name = name;
      this.value = value;
      this.signature = signature;
   }

   @Generated
   public Property() {
   }
}
