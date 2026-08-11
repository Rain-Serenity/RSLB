package com.rserene.chosen.server.skinrestorer;

import lombok.Generated;
import com.rserene.chosen.server.RSLB;
import com.rserene.chosen.server.skinrestorer.SkinRestorerResult;
import com.rserene.chosen.server.skinrestorer.SkinRestorerResult.Reason;
import com.rserene.chosen.server.profile.GameProfile;

public class SkinRestorerResultImpl implements SkinRestorerResult {
   private final Reason reason;
   private final GameProfile response;
   private final Throwable throwable;

   public static SkinRestorerResultImpl ofNoSkin() {
      return new SkinRestorerResultImpl(Reason.NO_SKIN, null, null);
   }

   public static SkinRestorerResultImpl ofNoRestorer() {
      return new SkinRestorerResultImpl(Reason.NO_RESTORER, null, null);
   }

   public static SkinRestorerResultImpl ofSignatureValid() {
      return new SkinRestorerResultImpl(Reason.SIGNATURE_VALID, null, null);
   }

   public static SkinRestorerResultImpl ofUseCache(GameProfile profile) {
      return new SkinRestorerResultImpl(Reason.USE_CACHE, profile, null);
   }

   public static SkinRestorerResultImpl ofRestorerSucceed(GameProfile profile) {
      return new SkinRestorerResultImpl(Reason.RESTORER_SUCCEED, profile, null);
   }

   public static SkinRestorerResultImpl ofBadSkin(Throwable throwable) {
      return new SkinRestorerResultImpl(Reason.BAD_SKIN, null, throwable);
   }

   public static SkinRestorerResultImpl ofRestorerFailed(Throwable throwable) {
      return new SkinRestorerResultImpl(Reason.RESTORER_FAILED, null, throwable);
   }

   public static void handleSkinRestoreResult(Throwable throwable) {
      RSLB.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "An exception occurred while processing the skin repair.", throwable);
   }

   public static void handleSkinRestoreResult(SkinRestorerResultImpl result) {
      if (result.getThrowable() != null) {
         handleSkinRestoreResult(result.getThrowable());
      }
   }

   @Generated
   public Reason getReason() {
      return this.reason;
   }

   @Generated
   public GameProfile getResponse() {
      return this.response;
   }

   @Generated
   public Throwable getThrowable() {
      return this.throwable;
   }

   @Generated
   private SkinRestorerResultImpl(Reason reason, GameProfile response, Throwable throwable) {
      this.reason = reason;
      this.response = response;
      this.throwable = throwable;
   }

   @Generated
   @Override
   public String toString() {
      return "SkinRestorerResultImpl(reason=" + this.getReason() + ", response=" + this.getResponse() + ", throwable=" + this.getThrowable() + ")";
   }
}
