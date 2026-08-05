package com.rserene.chosen.server.api.internal.util;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class There<V1, V2, V3> {
   private final V1 value1;
   private final V2 value2;
   private final V3 value3;

   @Generated
   public V1 getValue1() {
      return this.value1;
   }

   @Generated
   public V2 getValue2() {
      return this.value2;
   }

   @Generated
   public V3 getValue3() {
      return this.value3;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof There<?, ?, ?> other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         Object this$value1 = this.getValue1();
         Object other$value1 = other.getValue1();
         if (this$value1 == null ? other$value1 == null : this$value1.equals(other$value1)) {
            Object this$value2 = this.getValue2();
            Object other$value2 = other.getValue2();
            if (this$value2 == null ? other$value2 == null : this$value2.equals(other$value2)) {
               Object this$value3 = this.getValue3();
               Object other$value3 = other.getValue3();
               return this$value3 == null ? other$value3 == null : this$value3.equals(other$value3);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof There;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $value1 = this.getValue1();
      result = result * 59 + ($value1 == null ? 43 : $value1.hashCode());
      Object $value2 = this.getValue2();
      result = result * 59 + ($value2 == null ? 43 : $value2.hashCode());
      Object $value3 = this.getValue3();
      return result * 59 + ($value3 == null ? 43 : $value3.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "There(value1=" + this.getValue1() + ", value2=" + this.getValue2() + ", value3=" + this.getValue3() + ")";
   }

   @Generated
   public There(V1 value1, V2 value2, V3 value3) {
      this.value1 = value1;
      this.value2 = value2;
      this.value3 = value3;
   }
}
