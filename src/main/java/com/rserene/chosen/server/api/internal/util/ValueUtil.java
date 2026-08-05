package com.rserene.chosen.server.api.internal.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ValueUtil {
   public static byte[] uuidToBytes(UUID uuid) {
      byte[] uuidBytes = new byte[16];
      ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
      return uuidBytes;
   }

   public static UUID bytesToUuid(byte[] bytes) {
      if (bytes.length != 16) {
         return null;
      }

      int i = 0;
      long msl = 0L;

      while (i < 8) {
         msl = msl << 8 | bytes[i] & 0xFF;
         i++;
      }

      long lsl = 0L;

      while (i < 16) {
         lsl = lsl << 8 | bytes[i] & 0xFF;
         i++;
      }

      return new UUID(msl, lsl);
   }

   public static UUID getUuidOrNull(String uuid) {
      UUID ret = null;

      try {
         ret = UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
      } catch (Exception var3) {
      }

      return ret;
   }

   public static boolean isEmpty(String str) {
      return str == null || str.length() == 0;
   }

   public static String transPapi(String s, Pair<?, ?>... pairs) {
      for (int i = 0; i < pairs.length; i++) {
         s = s.replace("{" + pairs[i].getValue1() + "}", pairs[i].getValue2() + "");
         s = s.replace("{" + i + "}", pairs[i].getValue2() + "");
      }

      return s;
   }

   public static String transPapi(String s, List<Pair<?, ?>> pairs) {
      for (int i = 0; i < pairs.size(); i++) {
         s = s.replace("{" + pairs.get(i).getValue1() + "}", pairs.get(i).getValue2().toString());
         s = s.replace("{" + i + "}", pairs.get(i).getValue2().toString());
      }

      return s;
   }

   public static String join(CharSequence delimiter, CharSequence lastDelimiter, Object... elements) {
      if (elements.length == 0) {
         return "";
      }

      if (elements.length == 1) {
         return elements[0].toString();
      }

      StringJoiner joiner = new StringJoiner(delimiter);

      for (int i = 0; i < elements.length - 1; i++) {
         joiner.add(elements[i].toString());
      }

      return joiner.toString() + lastDelimiter + elements[elements.length - 1];
   }

   public static String join(CharSequence delimiter, CharSequence lastDelimiter, Collection<? extends Object> elements) {
      return join(delimiter, lastDelimiter, elements.toArray(new Object[0]));
   }

   public static byte[] sha256(String str) throws NoSuchAlgorithmException {
      return MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8));
   }

   public static UUID xuidToUUID(String xuid) {
      return new UUID(0L, Long.parseLong(xuid));
   }

   public static String generateLinkCode() {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < 6; i++) {
         builder.append((int)(10.0 * Math.random()));
      }

      return builder.toString();
   }

   /**
    * 透传反编译源码中保留下来的受检异常，避免改变原始字节码的异常传播语义。
    */
   public static RuntimeException sneakyThrow(Throwable throwable) {
      ValueUtil.<RuntimeException>sneakyThrow0(throwable);
      return null;
   }

   @SuppressWarnings("unchecked")
   private static <T extends Throwable> void sneakyThrow0(Throwable throwable) throws T {
      throw (T)throwable;
   }
}
