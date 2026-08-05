package com.rserene.chosen.server.core.auth.validate;

import lombok.Generated;
import com.rserene.chosen.server.api.profile.GameProfile;
import com.rserene.chosen.server.core.auth.service.BaseServiceAuthenticationResult;

public class ValidateContext {
   private final BaseServiceAuthenticationResult baseServiceAuthenticationResult;
   private final GameProfile inGameProfile;
   private String disallowMessage;
   private boolean needWait;
   private boolean onlineNameUpdated = false;

   protected ValidateContext(BaseServiceAuthenticationResult baseServiceAuthenticationResult) {
      this.baseServiceAuthenticationResult = baseServiceAuthenticationResult;
      this.inGameProfile = baseServiceAuthenticationResult.getResponse().clone();
   }

   @Generated
   public BaseServiceAuthenticationResult getBaseServiceAuthenticationResult() {
      return this.baseServiceAuthenticationResult;
   }

   @Generated
   public GameProfile getInGameProfile() {
      return this.inGameProfile;
   }

   @Generated
   public String getDisallowMessage() {
      return this.disallowMessage;
   }

   @Generated
   public boolean isNeedWait() {
      return this.needWait;
   }

   @Generated
   public boolean isOnlineNameUpdated() {
      return this.onlineNameUpdated;
   }

   @Generated
   public void setDisallowMessage(String disallowMessage) {
      this.disallowMessage = disallowMessage;
   }

   @Generated
   public void setNeedWait(boolean needWait) {
      this.needWait = needWait;
   }

   @Generated
   public void setOnlineNameUpdated(boolean onlineNameUpdated) {
      this.onlineNameUpdated = onlineNameUpdated;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ValidateContext other)) {
         return false;
      } else {
         if (!other.canEqual(this)) {
            return false;
         }

         if (this.isNeedWait() != other.isNeedWait()) {
            return false;
         }

         if (this.isOnlineNameUpdated() != other.isOnlineNameUpdated()) {
            return false;
         }

         Object this$baseServiceAuthenticationResult = this.getBaseServiceAuthenticationResult();
         Object other$baseServiceAuthenticationResult = other.getBaseServiceAuthenticationResult();
         if (this$baseServiceAuthenticationResult == null
            ? other$baseServiceAuthenticationResult == null
            : this$baseServiceAuthenticationResult.equals(other$baseServiceAuthenticationResult)) {
            Object this$inGameProfile = this.getInGameProfile();
            Object other$inGameProfile = other.getInGameProfile();
            if (this$inGameProfile == null ? other$inGameProfile == null : this$inGameProfile.equals(other$inGameProfile)) {
               Object this$disallowMessage = this.getDisallowMessage();
               Object other$disallowMessage = other.getDisallowMessage();
               return this$disallowMessage == null ? other$disallowMessage == null : this$disallowMessage.equals(other$disallowMessage);
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
      return other instanceof ValidateContext;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + (this.isNeedWait() ? 79 : 97);
      result = result * 59 + (this.isOnlineNameUpdated() ? 79 : 97);
      Object $baseServiceAuthenticationResult = this.getBaseServiceAuthenticationResult();
      result = result * 59 + ($baseServiceAuthenticationResult == null ? 43 : $baseServiceAuthenticationResult.hashCode());
      Object $inGameProfile = this.getInGameProfile();
      result = result * 59 + ($inGameProfile == null ? 43 : $inGameProfile.hashCode());
      Object $disallowMessage = this.getDisallowMessage();
      return result * 59 + ($disallowMessage == null ? 43 : $disallowMessage.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "ValidateContext(baseServiceAuthenticationResult="
         + this.getBaseServiceAuthenticationResult()
         + ", inGameProfile="
         + this.getInGameProfile()
         + ", disallowMessage="
         + this.getDisallowMessage()
         + ", needWait="
         + this.isNeedWait()
         + ", onlineNameUpdated="
         + this.isOnlineNameUpdated()
         + ")";
   }
}
