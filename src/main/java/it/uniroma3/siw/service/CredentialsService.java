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
        credentials.setRole(Credentials.DEFAULT_ROLE);
        
        // Se la password è NULL (es. utente Google), NON proviamo a criptarla.
        // Altrimenti il sistema va in crash come vedi nel log.
        if (credentials.getPassword() != null) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
        
        return this.credentialsRepository.save(credentials);
    }

    //Metodo per prendere tutte le credenziali
    @Transactional
    public Iterable<Credentials> getAllCredentials() {
        return this.credentialsRepository.findAll();
    }
    
    //Metodo per bannare
    public void lockCredentials(String username) {
        Credentials credentials = this.credentialsRepository.findByUsername(username).orElse(null);
        if (credentials.getPassword() != null) {
            credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
        }
            this.credentialsRepository.save(credentials);
        }
    

    //Metodo per riabilitare 
    public void unlockCredentials(String username) {
        Credentials credentials = this.credentialsRepository.findByUsername(username).orElse(null);
        if (credentials != null) {
            credentials.setEnabled(true); // RIAMMETTI L'UTENTE
            this.credentialsRepository.save(credentials);
        }
    }
}
