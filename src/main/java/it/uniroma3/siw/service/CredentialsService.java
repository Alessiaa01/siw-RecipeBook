package it.uniroma3.siw.service;

import java.util.Optional;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;

@Service
public class CredentialsService {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected CredentialsRepository credentialsRepository;

    @Transactional
    public Credentials getCredentials(Long id) {
        Optional<Credentials> result = this.credentialsRepository.findById(id);
        return result.orElse(null);
    }

    @Transactional
    public Credentials getCredentials(String username) {
        Optional<Credentials> result = this.credentialsRepository.findByUsername(username);
        return result.orElse(null);
    }

    @Transactional
    public Credentials saveCredentials(Credentials credentials) {
        // Imposta il ruolo di default 
        if (credentials.getRole() == null) {
            credentials.setRole(Credentials.DEFAULT_ROLE);
        }
        
        //Cifratura password
        if (credentials.getPassword() != null) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        
        return this.credentialsRepository.save(credentials);
    }

    @Transactional
    public List<Credentials> getAllCredentials() {
        return this.credentialsRepository.findAll();
    }
    
    // --- METODO BAN---
    @Transactional
    public void lockCredentials(String username) {
        Credentials credentials = this.credentialsRepository.findByUsername(username).orElse(null);
       
        // 1. Controlliamo se l'utente esiste 
        // 2. NON controlliamo la password, così possiamo bannare anche gli utenti Google
        if (credentials != null) {
            credentials.setEnabled(false); // Disabilita l'account
            this.credentialsRepository.save(credentials);
        }
    }

    // --- METODO UNBAN ---
    @Transactional
    public void unlockCredentials(String username) {
        Credentials credentials = this.credentialsRepository.findByUsername(username).orElse(null);
        
        if (credentials != null) {
            credentials.setEnabled(true); // Riabilita l'account
            this.credentialsRepository.save(credentials);
        }
    }
}
