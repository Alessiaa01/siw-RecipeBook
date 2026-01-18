package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;

@ControllerAdvice
public class GlobalController {

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private UserService userService;

    @ModelAttribute("currentUser") 
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // 1. LOGIN CLASSICO (Username/Password)
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            Credentials credentials = credentialsService.getCredentials(username);
            if (credentials != null) {
                return credentials.getUser();
            }
        } 
        
        // 2. LOGIN GOOGLE (OAuth2)
        else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");

            if (email != null) {
                // Cerchiamo l'utente nel DB
                User user = userService.getUserByEmail(email);
                
                // --- NOVITÀ: REGISTRAZIONE AUTOMATICA ---
                // Se l'utente non esiste, lo creiamo al volo!
                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    
                    // Recuperiamo nome e cognome da Google (se disponibili)
                    String name = oauth2User.getAttribute("given_name");
                    String surname = oauth2User.getAttribute("family_name");
                    
                    // Se Google non ci dà i nomi separati, usiamo l'email o un placeholder
                    user.setName(name != null ? name : "Utente");
                    user.setSurname(surname != null ? surname : "Google");
                    
                    // SALVIAMO IL NUOVO UTENTE NEL DB
                    this.userService.saveUser(user);
                }
                
                return user; 
            }
        }

        return null;
    }
}