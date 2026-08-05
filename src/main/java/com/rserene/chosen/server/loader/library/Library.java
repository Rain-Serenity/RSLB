package com.rserene.chosen.server.loader.library;

import lombok.Generated;

public class Library {
   private final String group;
   private final String name;
   private final String version;

   public static Library of(String value, String split) {
      String[] args = value.split(split);
      return new Library(args[0], args[1], args[2]);
   }

   public String getFileName() {
      return String.format("%s-%s.jar", this.name, this.version);
   }

   public String getDownloadUrl() {
      return this.group.replace(".", "/") + "/" + this.name + "/" + this.version + "/" + this.name + "-" + this.version + ".jar";
   }

   @Generated
   public Library(String group, String name, String version) {
      this.group = group;
      this.name = name;
      this.version = version;
   }

   @Generated
   public String getGroup() {
      return this.group;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getVersion() {
      return this.version;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Library other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         Object this$group = this.getGroup();
         Object other$group = other.getGroup();
         if (this$group == null ? other$group == null : this$group.equals(other$group)) {
            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null ? other$name == null : this$name.equals(other$name)) {
               Object this$version = this.getVersion();
               Object other$version = other.getVersion();
               return this$version == null ? other$version == null : this$version.equals(other$version);
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
      return other instanceof Library;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $group = this.getGroup();
      result = result * 59 + ($group == null ? 43 : $group.hashCode());
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $version = this.getVersion();
      return result * 59 + ($version == null ? 43 : $version.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "Library(group=" + this.getGroup() + ", name=" + this.getName() + ", version=" + this.getVersion() + ")";
   }
}
