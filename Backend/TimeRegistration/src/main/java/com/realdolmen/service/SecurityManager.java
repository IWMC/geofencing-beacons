package com.realdolmen.service;

import com.realdolmen.TestMode;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.jsf.UserContext;
import com.realdolmen.json.JsonWebToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * A stateless EJB used as a collection of security functions.
 */
@Singleton
public class SecurityManager {

    private final Key key = MacProvider.generateKey();

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager entityManager;

    @Inject
    private HttpServletRequest request;

    @Inject
    private UserContext userContext;

    public String randomSalt() throws NoSuchAlgorithmException {
        return new BigInteger(32 * 8, SecureRandom.getInstanceStrong()).toString(32);
    }

    public String generateHash(String salt, String clearText) throws NoSuchAlgorithmException {
        return new String(Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest((salt + clearText).getBytes())), Charset.forName("UTF-8"));
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
        } catch (Exception se) {
            return false;
        }

        return true;
    }

    public Employee findEmployee() {
        if (TestMode.isTestMode()) {
            return Optional.ofNullable(userContext == null ? null : userContext.getUser()).orElse(request == null ? null : findByJwt(new JsonWebToken(request.getHeader("Authorization"))));
        } else {
            return Optional.ofNullable(userContext.getUser()).orElse(findByJwt(new JsonWebToken(request.getHeader("Authorization"))));
        }
    }

    @Nullable
    public Employee findByJwt(@NotNull JsonWebToken jwt) {
        if (jwt == null || jwt.getToken() == null || jwt.getToken().isEmpty()) {
            return null;
        }

        try {
            long id = Long.parseLong(Jwts.parser().setSigningKey(key).parseClaimsJws(jwt.getToken()).getBody().get("id")
                    .toString());
            return entityManager.find(Employee.class, id);
        } catch (MalformedJwtException | NoResultException | SignatureException ex) {
            return null;
        }
    }

    public boolean isManagementEmployee() {
        Employee employee = findEmployee();
        return employee != null && employee instanceof ManagementEmployee;
    }

    public boolean isProjectManager() {
        Employee employee = findEmployee();
        return employee != null && employee instanceof ProjectManager;
    }

    public boolean isManagement() {
        Employee employee = findEmployee();
        return employee != null && (employee instanceof ProjectManager || employee instanceof ManagementEmployee);
    }

    @TestOnly
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }
}
