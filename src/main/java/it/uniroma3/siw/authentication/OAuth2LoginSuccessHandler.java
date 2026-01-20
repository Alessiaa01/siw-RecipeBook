package it.uniroma3.siw.authentication;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    //questo metodo scatta immediatamente dopo che il login di Google si è chiusa con successo
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
    	
    	// --- SPIA 1: SIAMO ENTRATI NEL METODO? ---
        System.out.println("DEBUG: 1. OAuth2 Success Handler attivato!");

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = token.getPrincipal();
        
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("given_name");
        String surname = oauthUser.getAttribute("family_name");
        
     // --- SPIA 2: ABBIAMO I DATI? ---
        System.out.println("DEBUG: 2. Dati ricevuti da Google: " + email + " - " + name + " - " + surname);
        
        // salva solo se non esiste
        userService.processOAuthPostLogin(email, name, surname);
        
        if (email != null) {
            userService.processOAuthPostLogin(email, name, surname);
        } else {
             System.out.println("DEBUG: ERRORE GRAVE - L'email è NULL!");
        }

        // Reindirizza alla pagina di successo (o dove vuoi tu)
        this.setDefaultTargetUrl("/success");
        //chiama il metodo della classe genitore (SavedRequest...), perchè se l'utente prima di loggarsi voleva andare in una
        //determinata pagina, viene mandato direttamente lì. Ignora /success
        super.onAuthenticationSuccess(request, response, authentication);
    }
}