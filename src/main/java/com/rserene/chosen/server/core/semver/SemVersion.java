package com.rserene.chosen.server.core.semver;

import java.util.Locale;
import lombok.Generated;
import com.rserene.chosen.server.api.internal.util.ValueUtil;

public class SemVersion {
   private final int major;
   private final int minor;
   private final int patch;
   private final SemVersion.VersionSuffix suffixes;
   private final int suffixesBd;

   public static SemVersion of(String version) {
      if (ValueUtil.isEmpty(version)) {
         return null;
      } else if (version.toLowerCase(Locale.ROOT).startsWith("build_")) {
         return null;
      } else {
         String[] split = version.split("-");
         String[] mmp = split[0].split("\\.");
         if (split.length == 1) {
            return new SemVersion(Integer.parseInt(mmp[0]), Integer.parseInt(mmp[1]), Integer.parseInt(mmp[2]), SemVersion.VersionSuffix.NONE, -1);
         } else if (split.length == 2) {
            split = split[1].split("\\.");
            return new SemVersion(
               Integer.parseInt(mmp[0]),
               Integer.parseInt(mmp[1]),
               Integer.parseInt(mmp[2]),
               SemVersion.VersionSuffix.valueOf(split[0]),
               Integer.parseInt(split[1])
            );
         } else {
            return null;
         }
      }
   }

   @Override
   public String toString() {
      return this.suffixes == SemVersion.VersionSuffix.NONE
         ? String.format("%d.%d.%d", this.major, this.minor, this.patch)
         : String.format("%d.%d.%d-%s.%d", this.major, this.minor, this.patch, this.suffixes.name(), this.suffixesBd);
   }

   public boolean needUpgrade(SemVersion version) {
      if (version.suffixes.mj < this.suffixes.mj) {
         return false;
      }

      if (version.major == this.major && version.minor == this.minor && version.patch == this.patch) {
         if (version.suffixes.mj > this.suffixes.mj) {
            return true;
         }

         if (version.suffixesBd > this.suffixesBd) {
            return true;
         }
      }

      return this.needUpgradeIgnoreSuffixes(version);
   }

   public boolean needUpgradeIgnoreSuffixes(SemVersion version) {
      return version.major >= this.major && version.minor >= this.minor && version.patch > this.patch;
   }

   @Generated
   private SemVersion(int major, int minor, int patch, SemVersion.VersionSuffix suffixes, int suffixesBd) {
      this.major = major;
      this.minor = minor;
      this.patch = patch;
      this.suffixes = suffixes;
      this.suffixesBd = suffixesBd;
   }

   @Generated
   public int getMajor() {
      return this.major;
   }

   @Generated
   public int getMinor() {
      return this.minor;
   }

   @Generated
   public int getPatch() {
      return this.patch;
   }

   @Generated
   public SemVersion.VersionSuffix getSuffixes() {
      return this.suffixes;
   }

   @Generated
   public int getSuffixesBd() {
      return this.suffixesBd;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SemVersion other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         if (this.getMajor() != other.getMajor()) {
            return false;
         }

         if (this.getMinor() != other.getMinor()) {
            return false;
         }

         if (this.getPatch() != other.getPatch()) {
            return false;
         }

         if (this.getSuffixesBd() != other.getSuffixesBd()) {
            return false;
         }

         Object this$suffixes = this.getSuffixes();
         Object other$suffixes = other.getSuffixes();
         return this$suffixes == null ? other$suffixes == null : this$suffixes.equals(other$suffixes);
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof SemVersion;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getMajor();
      result = result * 59 + this.getMinor();
      result = result * 59 + this.getPatch();
      result = result * 59 + this.getSuffixesBd();
      Object $suffixes = this.getSuffixes();
      return result * 59 + ($suffixes == null ? 43 : $suffixes.hashCode());
   }

   enum VersionSuffix {
      NONE(3),
      RC(2),
      BETA(1),
      ALPHA(0);

      private final int mj;

      VersionSuffix(int mj) {
         this.mj = mj;
      }
   }
}
