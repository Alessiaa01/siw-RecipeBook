package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;

@ControllerAdvice
public class GlobalController {

    @Autowired
    private CredentialsService credentialsService;

    @ModelAttribute("currentUser") 
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 1. Se l'utente non è loggato, ritorna null
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        // 2. Cerchiamo lo username (o l'email)
        String username = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            // Login Classico
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            // Login con Google/Facebook
            // Nota: qui dipende da come hai salvato l'utente OAuth nel DB. 
            // Di solito si usa l'email come identificativo.
            username = ((OAuth2User) principal).getAttribute("email");
        }

        // 3. Recuperiamo l'utente vero dal DB usando il service
        // (Assumiamo che il tuo credentialsService abbia un metodo per recuperare le credenziali o l'utente)
        try {
            if (username != null) {
                // Recupera le credenziali e da lì l'utente
                return credentialsService.getCredentials(username).getUser();
            }
        } catch (Exception e) {
            // Gestione caso in cui l'utente non esista nel DB (improbabile se loggato)
            return null;
        }

        return null;
    }
}