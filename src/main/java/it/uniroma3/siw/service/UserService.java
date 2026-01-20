package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.repository.CredentialsRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The UserService handles logic for Users.
 */
@Service
public class UserService {

    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected CredentialsRepository credentialsRepository;

    /**
     * This method retrieves a User from the DB based on its ID.
     * @param id the id of the User to retrieve from the DB
     * @return the retrieved User, or null if no User with the passed ID could be found in the DB
     */
    @Transactional
    public User getUser(Long id) {
        Optional<User> result = this.userRepository.findById(id);
        return result.orElse(null);
    }

    /**
     * This method saves a User in the DB.
     * @param user the User to save into the DB
     * @return the saved User
     * @throws DataIntegrityViolationException if a User with the same username
     *                              as the passed User already exists in the DB
     */
    @Transactional
    public User saveUser(User user) {
        return this.userRepository.save(user);
    }
    
    

    /**
     * This method retrieves all Users from the DB.
     * @return a List with all the retrieved Users
     */
    @Transactional
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        Iterable<User> iterable = this.userRepository.findAll();
        for(User user : iterable)
            result.add(user);
        return result;
    }
    public User getUserByEmail(String email) {
    	return userRepository.findByEmail(email).orElse(null);
    }
    
 // Nel UserService.java

    @Transactional
    public void processOAuthPostLogin(String email, String name, String surname) {
        
        // 1. Controlliamo se esistono le CREDENZIALI (è questo che conta per il login e per l'admin)
        Optional<Credentials> existCred = credentialsRepository.findByUsername(email);

        // Se le credenziali NON esistono, dobbiamo crearle
        if (existCred.isEmpty()) {
            System.out.println("DEBUG: Credenziali non trovate. Procedo alla creazione...");

            // 2. Controlliamo se esiste già l'UTENTE (per evitare duplicati o errori di unique constraint)
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

            // 3. Creiamo finalmente le Credenziali e le colleghiamo
            Credentials credentials = new Credentials();
            credentials.setUsername(email);
            credentials.setPassword(null);
            credentials.setRole(Credentials.DEFAULT_ROLE);
            credentials.setEnabled(true); // Fondamentale!
            credentials.setUser(userToLink);
            
            credentialsRepository.save(credentials);
            System.out.println("DEBUG: Credenziali salvate e collegate!");
        } else {
            System.out.println("DEBUG: L'utente è già registrato e completo. Login standard.");
        }
    }
    
}
        
    


    

