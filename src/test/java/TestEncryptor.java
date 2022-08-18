import com.github.exadmin.mpcr.misc.MyEncryptor;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestEncryptor {
    @Test
    public void justEncryptTest() {
        String sourceText = "Hello World!";
        String passPhrase = "Very long secret string with special characters #!@#!";

        String encrypted = MyEncryptor.encrypt(sourceText, passPhrase);
        assertNotNull(encrypted);

        assertNotEquals(sourceText, encrypted);

        System.out.println(encrypted);
    }

    @Test
    public void justEncryptTest2() {
        String sourceText = "Hello World!Hello World!Hello World!Hello World!Hello World!";
        String passPhrase = "Very long secret string with special characters #!@#!";

        String encrypted = MyEncryptor.encrypt(sourceText, passPhrase);
        assertNotNull(encrypted);

        assertNotEquals(sourceText, encrypted);
        System.out.println(encrypted);
    }

    @Test
    public void basicEncryptDecryptTest() {
        String sourceText = "Hello World!Hello World!Hello World!Hello World!Hello World!";
        String passPhrase = "Very long secret string with special characters #!@#!";

        String encryptedStr = MyEncryptor.encrypt(sourceText, passPhrase);
        assertNotNull(encryptedStr);

        assertNotEquals(sourceText, encryptedStr);
        System.out.println(encryptedStr);

        String decryptedStr = MyEncryptor.decrypt(encryptedStr, passPhrase);
        assertNotNull(decryptedStr);

        assertEquals(sourceText, decryptedStr);
    }

    @Test
    public void basicEncryptDecryptTestShortPassword() {
        String sourceText = "Hello World!Hello World!Hello World!Hello World!Hello World!";
        String passPhrase = "!32";

        String encryptedStr = MyEncryptor.encrypt(sourceText, passPhrase);
        assertNotNull(encryptedStr);

        assertNotEquals(sourceText, encryptedStr);
        System.out.println(encryptedStr);

        String decryptedStr = MyEncryptor.decrypt(encryptedStr, passPhrase);
        assertNotNull(decryptedStr);

        assertEquals(sourceText, decryptedStr);
    }

    @Test
    public void basicEncryptDecryptTestLongPassword() {
        String sourceText = "Hello World!Hello World!Hello World!Hello World!Hello World!";
        String passPhrase = "!3282nrc982yr3c982 y398hf2093dh0932hdo9u32hd 092y3d 982y39hod y32o97d giwyfgc i732yd9732 udgiiywfeg i8273u gfi823 gfi82iy3gid8y32g di823ygqd i82iy3gd o723qgdi83";

        String encryptedStr = MyEncryptor.encrypt(sourceText, passPhrase);
        assertNotNull(encryptedStr);

        assertNotEquals(sourceText, encryptedStr);
        System.out.println(encryptedStr);

        String decryptedStr = MyEncryptor.decrypt(encryptedStr, passPhrase);
        assertNotNull(decryptedStr);

        assertEquals(sourceText, decryptedStr);
    }

    @Test
    public void basicEncryptDecryptUsingWrongPassword() {
        String sourceText = ">>> Hello World!Hello World!Hello World!Hello World!Hello World! <<<";
        String passPhraseGood = "correct password";
        String passPhraseBad = "wrong password";

        String encryptedStr = MyEncryptor.encrypt(sourceText, passPhraseGood);
        assertNotNull(encryptedStr);

        assertNotEquals(sourceText, encryptedStr);
        System.out.println(encryptedStr);

        String result = MyEncryptor.decrypt(encryptedStr, passPhraseBad);
        assertNull(result); // we are expecting null as password was incorrect
    }
}
