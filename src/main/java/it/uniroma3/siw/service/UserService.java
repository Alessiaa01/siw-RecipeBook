package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;


import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected CredentialsRepository credentialsRepository;
    
    //salva un utente nel DB   
    @Transactional
    public User saveUser(User user) {
        return this.userRepository.save(user);
    }
   
    //la lista di tutti gli utenti registrati
    @Transactional
    public List<User> getAllUsers() {
       return  this.userRepository.findAll();
       
    }
    public User getUserByEmail(String email) {
    	return userRepository.findByEmail(email).orElse(null);
    }
    
    public User findById(Long id) {
    	return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void processOAuthPostLogin(String email, String name, String surname) {
        
        // 1. Controlliamo se esistono le CREDENZIALI 
        Optional<Credentials> existCred = credentialsRepository.findByUsername(email);

        // Se le credenziali NON esistono, dobbiamo crearle
        if (existCred.isEmpty()) {
            System.out.println("DEBUG: Credenziali non trovate. Procedo alla creazione...");

            //  Controlliamo se esiste già l'UTENTE (per evitare duplicati )
            Optional<User> existUser = userRepository.findByEmail(email);
            User userToLink;

            if (existUser.isPresent()) {
                // Caso "Orfano": L'utente c'era già, usiamo quello esistente
                System.out.println("DEBUG: Trovato Utente esistente senza credenziali. Lo riutilizzo.");
                userToLink = existUser.get();
                // Aggiorniamo i dati nel caso siano cambiati su Google
                userToLink.setName(name);
                userToLink.setSurname(surname);
                userRepository.save(userToLink);
            } else {
                // Caso "Nuovo Totale": Creiamo un nuovo utente da zero
                System.out.println("DEBUG: Creazione nuovo Utente.");
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name != null ? name : "Utente");
                newUser.setSurname(surname != null ? surname : "Google");
                userToLink = userRepository.save(newUser);
            }

            // Creiamo le Credenziali e le colleghiamo
            Credentials credentials = new Credentials();
            credentials.setUsername(email);
            credentials.setPassword(null);
            credentials.setRole(Credentials.DEFAULT_ROLE);
            credentials.setEnabled(true); 
            credentials.setUser(userToLink);//colleghiamo il login(email) al persona fisica
            
            credentialsRepository.save(credentials);
            System.out.println("DEBUG: Credenziali salvate e collegate!");
        } else {
            System.out.println("DEBUG: L'utente è già registrato e completo. Login standard.");
        }
    }
    
}
        
    


    

