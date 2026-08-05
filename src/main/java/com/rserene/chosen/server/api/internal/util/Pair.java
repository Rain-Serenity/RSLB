package com.rserene.chosen.server.api.internal.util;

import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Pair<V1, V2> {
   private final V1 value1;
   private final V2 value2;

   @Generated
   public V1 getValue1() {
      return this.value1;
   }

   @Generated
   public V2 getValue2() {
      return this.value2;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Pair<?, ?> other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$value1 = this.getValue1();
         Object other$value1 = other.getValue1();
         if (this$value1 == null ? other$value1 == null : this$value1.equals(other$value1)) {
            Object this$value2 = this.getValue2();
            Object other$value2 = other.getValue2();
            return this$value2 == null ? other$value2 == null : this$value2.equals(other$value2);
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Pair;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $value1 = this.getValue1();
      result = result * 59 + ($value1 == null ? 43 : $value1.hashCode());
      Object $value2 = this.getValue2();
      return result * 59 + ($value2 == null ? 43 : $value2.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "Pair(value1=" + this.getValue1() + ", value2=" + this.getValue2() + ")";
   }

   @Generated
   public Pair(V1 value1, V2 value2) {
      this.value1 = value1;
      this.value2 = value2;
   }
}
