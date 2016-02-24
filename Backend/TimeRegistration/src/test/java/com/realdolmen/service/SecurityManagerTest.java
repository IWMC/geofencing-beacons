package com.realdolmen.service;

import com.realdolmen.entity.Employee;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by BCCAZ45 on 24/02/2016.
 */
public class SecurityManagerTest {

    private SecurityManager securityManager;
    private Employee employee;

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
        System.out.println(generatedHash + " for salt: " + employee.getSalt() + " and pass: " + employee.getPassword());
        assertEquals(expectedHash, generatedHash);
    }

    @Test
    public void testCheckPassword() throws Exception {
        employee.setHash(securityManager.generateHash(employee.getSalt(), employee.getPassword()));
        assertTrue("Correct password returns true", securityManager.checkPassword(employee, employee.getPassword()));
        assertFalse("Invalid password returns false", securityManager.checkPassword(employee, "Bad password"));
    }

    @Test
    public void testGenerateToken() throws Exception {
        String firstToken = securityManager.generateToken(employee);
        String secondToken = securityManager.generateToken(employee);
        Jwts.parser().setSigningKey(securityManager.getKey()).parseClaimsJws(firstToken);
    }

    @Test(expected = SignatureException.class)
    public void testGenerateTokenInvalid() throws Exception {
        String badToken = Jwts.builder().setSubject(employee.getUsername()).claim("id", employee.getId())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "invalidkey").compact();
        Jwts.parser().setSigningKey(securityManager.getKey()).parseClaimsJws(badToken);
    }
}