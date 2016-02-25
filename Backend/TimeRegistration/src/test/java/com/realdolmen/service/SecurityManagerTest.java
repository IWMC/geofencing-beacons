package com.realdolmen.service;

import com.realdolmen.entity.Employee;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Spy;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by BCCAZ45 on 24/02/2016.
 */
public class SecurityManagerTest {

    private SecurityManager securityManager;
    private Employee employee;

    @Spy
    private Key jwtKey = MacProvider.generateKey();

    @Before
    public void setUp() throws Exception {
        securityManager = new SecurityManager();
        employee = new Employee("test", "asalt", null, "bla@bla.com", "testuser", "user", "test");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRandomSalt() throws Exception {
        String firstSalt = securityManager.randomSalt();
        String secondSalt = securityManager.randomSalt();

        assertNotNull(firstSalt);
        assertNotNull(secondSalt);
        assertTrue("Salt length is larger than 0", firstSalt.length() > 0);
        assertTrue("Salt length is larger than 0", secondSalt.length() > 0);
        System.out.println("Performing 1000 generations for salt randomness");
        for (int i = 0; i < 1000; i++) {
            firstSalt = securityManager.randomSalt();
            secondSalt = securityManager.randomSalt();
            assertNotEquals("Random salts are not the same after " + i + " runs", firstSalt, secondSalt);
        }
    }

    @Test
    public void testGenerateHash() throws Exception {
        String generatedHash = securityManager.generateHash(employee.getSalt(), employee.getPassword());
        String expectedHash = new String(MessageDigest.getInstance("SHA-256").digest((employee.getSalt() + employee.getPassword()).getBytes()), Charset.forName("UTF-8"));
        System.out.println("hash in base64: " + Base64.getEncoder().encodeToString(generatedHash.getBytes()) + " for salt: " + employee.getSalt() + " and pass: " + employee.getPassword());
        assertEquals(expectedHash, generatedHash);
    }

    @Test
    public void testCheckPassword() throws Exception {
        employee.setHash(securityManager.generateHash(employee.getSalt(), employee.getPassword()));
        assertTrue("Correct password returns true", securityManager.checkPassword(employee, employee.getPassword()));
        assertFalse("Invalid password returns false", securityManager.checkPassword(employee, "Bad password"));
    }

    @Test
    public void testGenerateTokenReturnsValidToken() throws Exception {
        String firstToken = null;
        System.out.println("Performing 1000 jwt token generations");
        for (int i = 0; i < 1000; i++) {
            firstToken = securityManager.generateToken(employee);
            Thread.sleep(2);
            String secondToken = securityManager.generateToken(employee);
            assertNotEquals(firstToken, secondToken);
        }

        assertNotNull(Jwts.parser().setSigningKey(securityManager.getKey()).parseClaimsJws(firstToken).getBody().getIssuedAt());
    }

    @Test(expected = SignatureException.class)
    public void testInvalidTokenIsInvalidated() throws Exception {
        long timestamp = System.currentTimeMillis();
        String badToken = Jwts.builder().setSubject(employee.getUsername()).claim("id", employee.getId())
                .claim("timestamp", timestamp)
                .setIssuedAt(new Date(timestamp))
                .signWith(SignatureAlgorithm.HS256, MacProvider.generateKey()).compact();
        assertFalse(securityManager.isValidToken(badToken));
        Jwts.parser().setSigningKey(securityManager.getKey()).parseClaimsJws(badToken);
    }
}