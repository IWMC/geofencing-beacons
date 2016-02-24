package com.realdolmen.service;

import com.realdolmen.entity.Employee;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.ejb.Singleton;
import javax.ejb.Stateless;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A stateless EJB used as a collection of security functions.
 */
@Singleton
public class SecurityManager {

    private final Key key = MacProvider.generateKey();

    public String randomSalt() throws NoSuchAlgorithmException {
        return new BigInteger(32 * 8, SecureRandom.getInstanceStrong()).toString(32);
    }

    public String generateHash(String salt, String clearText) throws NoSuchAlgorithmException {
        return new String(MessageDigest.getInstance("SHA-256").digest((salt + clearText).getBytes()), Charset.forName("UTF-8"));
    }

    public boolean checkPassword(Employee employee, String password) throws NoSuchAlgorithmException {
        return generateHash(employee.getSalt(), password).equals(employee.getHash());
    }

    public String generateToken(Employee employee) {
        return Jwts.builder().setSubject(employee.getUsername()).claim("id", employee.getId())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, key).compact();
    }
}
