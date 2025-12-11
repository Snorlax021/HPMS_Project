package Util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Arrays;

/**
 * PBKDF2 password hashing util using PBKDF2WithHmacSHA256.
 *
 * Stored format: pbkdf2_sha256$iterations$base64(salt)$base64(hash)
 */
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int DEFAULT_ITER = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final SecureRandom RAND = new SecureRandom();

    private PasswordHasher() {}

    public static String hash(char[] password) { return hash(password, DEFAULT_ITER); }

    public static String hash(char[] password, int iterations) {
        byte[] salt = new byte[SALT_BYTES];
        RAND.nextBytes(salt);
        byte[] dk = pbkdf2(password, salt, iterations, KEY_LENGTH);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String encodedHash = Base64.getEncoder().encodeToString(dk);
        Arrays.fill(dk, (byte)0);
        return String.format("pbkdf2_sha256$%d$%s$%s", iterations, encodedSalt, encodedHash);
    }

    public static boolean verify(char[] password, String stored) {
        if (password == null || stored == null || !stored.startsWith("pbkdf2_sha256$")) return false;
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 4) return false;
            int iter = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iter, expected.length * 8);
            boolean match = MessageDigest.isEqual(expected, actual);
            Arrays.fill(actual, (byte)0);
            return match;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] res = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return res;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error while hashing a password", e);
        }
    }
}