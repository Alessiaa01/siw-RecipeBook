package it.uniroma3.siw.service;

import java.util.Optional;

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
        // Imposta il ruolo di default (DEFAULT_ROLE) solo se non è già settato
        // (Opzionale: dipende se vuoi permettere salvataggi di admin)
        if (credentials.getRole() == null) {
            credentials.setRole(Credentials.DEFAULT_ROLE);
        }
        
        // CORRETTO: Gestione utenti Google (password null)
        if (credentials.getPassword() != null) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        
        return this.credentialsRepository.save(credentials);
    }

    @Transactional
    public Iterable<Credentials> getAllCredentials() {
        return this.credentialsRepository.findAll();
    }
    
    // --- METODO BAN CORRETTO ---
    @Transactional
    public void lockCredentials(String username) {
        Credentials credentials = this.credentialsRepository.findByUsername(username).orElse(null);
        
        // Correggiamo la logica:
        // 1. Controlliamo se l'utente esiste (credentials != null)
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
