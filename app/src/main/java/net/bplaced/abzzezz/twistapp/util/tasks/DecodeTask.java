/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import net.bplaced.abzzezz.twistapp.util.misc.KeyUtil;
import net.bplaced.abzzezz.twistapp.util.misc.StringHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;

public class DecodeTask extends TaskExecutor implements Callable<String> {


    private final String item;

    public DecodeTask(final String item) {
        this.item = item;
    }


    public <R> void executeAsync(final Callback<String> callback) {
        super.executeAsync(this, callback);
    }


    @Override
    public String call() throws Exception {
        final byte[] sourceDecoded = Base64.getDecoder().decode(item);
        final byte[] salt = Arrays.copyOfRange(sourceDecoded, 8, 16);
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final byte[][] keyAndIV = KeyUtil.GenerateKeyAndIV(32, 16, 1, salt, StringHandler.KEY.getBytes(StandardCharsets.UTF_8), md5);
        final SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
        final IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);
        final byte[] encrypted = Arrays.copyOfRange(sourceDecoded, 16, sourceDecoded.length);
        final Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            final byte[] decryptedData = aesCBC.doFinal(encrypted);
            return StringHandler.STREAM_URL + new String(decryptedData, StandardCharsets.UTF_8);
        } catch (InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return "-1";
        }
    }

}
