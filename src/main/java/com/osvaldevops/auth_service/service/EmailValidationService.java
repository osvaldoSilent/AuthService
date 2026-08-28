package com.osvaldevops.auth_service.service;

import org.springframework.stereotype.Service;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import java.util.Hashtable;
import java.util.Set;

@Service
public class EmailValidationService {
    // Lista negra de dominios de correos desechables comunes
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "10minutemail.com", "tempmail.com", "guerrillamail.com", "yopmail.com", "trashmail.com"
    );

    public boolean isEmailRealAndValid(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();

        // 1. Bloqueo de correos desechables
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            return false;
        }

        //TO-DO: to use hasMxRecords to verify mxRecords (probably to use factory pattern to validate multiple regions) 
        return true;
    }

    //private boolean hasMxRecords(String domain) {
        //try {
            //Hashtable<String, String> env = new Hashtable<>();
            //env.put("java.naming.factory.initial", "com.sun.jndi.dns.DNSContextFactory");
            
            //DirContext ictx = new InitialDirContext(env);
            //Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});
            //Attribute attr = attrs.get("MX");

            //return (attr != null && attr.size() > 0);
        //} catch (Exception e) {
            // El dominio no existe o no tiene servidor de correo configurado
            //return false;
        //}
    //}
}
