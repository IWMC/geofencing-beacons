package com.realdolmen.service;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.json.JsonWebToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.jetbrains.annotations.Nullable;

import javax.ejb.Singleton;
import javax.json.Json;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/**
 * A stateless EJB used as a collection of security functions.
 */
@Singleton
public class SecurityManager {

    private final Key key = MacProvider.generateKey();

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION_UNIT)
    private EntityManager entityManager;

    public String randomSalt() throws NoSuchAlgorithmException {
        return new BigInteger(32 * 8, SecureRandom.getInstanceStrong()).toString(32);
    }

    public String generateHash(String salt, String clearText) throws NoSuchAlgorithmException {
        return new String(MessageDigest.getInstance("SHA-256").digest((salt + clearText).getBytes()), Charset.forName("UTF-8"));
    }

    public boolean checkPassword(Employee employee, String password) throws NoSuchAlgorithmException {
        return generateHash(employee.getSalt(), password).equals(employee.getHash());
    }

    public JsonWebToken generateToken(Employee employee) {
        long timestamp = System.currentTimeMillis();
        return new JsonWebToken(Jwts.builder().setSubject(employee.getUsername()).claim("id", employee.getId())
                .claim("timestamp", timestamp)
                .setIssuedAt(new Date(timestamp))
                .signWith(SignatureAlgorithm.HS256, key).compact());
    }

    public Key getKey() {
        return key;
    }

    public boolean isValidToken(@NotNull JsonWebToken jwtToken) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(jwtToken.getToken());
        } catch (SignatureException se) {
            return false;
        }

        return true;
    }

    @Nullable
    public Employee findByJwt(@NotNull JsonWebToken jwt) {
        try {
            long id = Long.parseLong(Jwts.parser().setSigningKey(key).parseClaimsJws(jwt.getToken()).getBody().get("id")
                    .toString());
            return entityManager.find(Employee.class, id);
        } catch (NoResultException | SignatureException ex) {
            return null;
        }
    }
}
