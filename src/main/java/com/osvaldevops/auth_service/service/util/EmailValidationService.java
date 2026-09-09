package com.osvaldevops.auth_service.service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.NonNull;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import java.util.Hashtable;
import java.util.Set;

@Service
public class EmailValidationService {

    private final boolean isDnsValidationEnabled;

    public EmailValidationService(@Value("${app.security.validation.dns.enabled:true}") boolean isDnsValidationEnabled) {
        this.isDnsValidationEnabled = isDnsValidationEnabled;
    }
    
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "10minutemail.com", "tempmail.com", "guerrillamail.com", "yopmail.com", "trashmail.com"
    );

    public boolean isEmailRealAndValid(@NonNull String email) {
        boolean isEmailRealAndValid;
        if (email == null || !email.contains("@")) {
            isEmailRealAndValid = false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();

        // 1. Bloqueo de correos desechables
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            return false;
        }
        if (!isDnsValidationEnabled) {
            System.out.println("⚠️ DNS Validation by-passed (Feature Flag disabled). Aprobando: " + domain);
            return true; 
        }
        isEmailRealAndValid = hasMxRecords(domain);
        return isEmailRealAndValid;
    }

    private boolean hasMxRecords(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DNSContextFactory");
            
            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");

            return (attr != null && attr.size() > 0);
        } catch (Exception e) {
            System.err.println("❌ Fallo en JNDI/DNS para " + domain + ": " + e.getMessage());
            return false;
        }
    }
}
