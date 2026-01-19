package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;

import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.UUID;


@Controller
public class AuthenticationController {
	
	@Autowired
	private CredentialsService credentialsService;

    @Autowired
	private UserService userService;
	
	// Mostra il form di registrazione
	@GetMapping("/register") 
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "formRegisterUser"; // 
	}
	
	// Mostra il form di login
	@GetMapping("/login") 
	public String showLoginForm() {
		return "formLogin"; // template Thymeleaf
	}
/*
	// Home page (index)
	@GetMapping("/") 
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken) {
			return "redirect:/recipes"; 
		} else {		
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
			if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
				// se è admin, vai alla dashboard admin
				//return "admin/indexAdmin.html";
				return "redirect:/admin";
			} else {
				// se è utente normale, vai alla lista ricette
				return "redirect:/recipes";
			}
		}
	}
		
	
/*
		@GetMapping("/") 
		public String index(Model model) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication instanceof AnonymousAuthenticationToken) {
				return "redirect:/recipes"; 
			} else {		
				UserDetails userDetails = (UserDetails) authentication.getPrincipal();
				Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
				if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
					// MODIFICA: Reindirizza direttamente a manageRecipes (saltando indexAdmin)
					return "redirect:/admin/manageRecipes"; 
				} else {
					// se è utente normale, vai alla lista ricette
					return "redirect:/recipes";
				}
			}
		}
	*/	
	
   //dove mandare l'utente 
	@GetMapping(value = "/success")
	public String defaultAfterLogin(Model model) {
		//chi è l'utente connesso?
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    // CASO 1: Login con GOOGLE (OAuth2)
	    if (authentication instanceof OAuth2AuthenticationToken) {
	        // NON FARE NULLA QUI! L'utente è già stato salvato dal SuccessHandler.
	        // Devi solo reindirizzare.
	        return "redirect:/recipes";
	    }

	    // CASO 2: Login CLASSICO (Username/Password)
	    else {
	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
	        
	        if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
	            return "redirect:/admin/manageRecipes"; // O ovunque vada l'admin
	        }
	        return "redirect:/recipes";
	    }
	}
	    
	
		// Registrazione utente
	@PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult userBindingResult,
                               @Valid @ModelAttribute("credentials") Credentials credentials,
                               BindingResult credentialsBindingResult,
                               Model model) {

		if (!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
            // salva l'utente
            userService.saveUser(user);
            credentials.setUser(user);
            credentialsService.saveCredentials(credentials);
            model.addAttribute("user", user);
            return "registrationSuccessful"; // pagina conferma registrazione
        }
        return "formRegisterUser"; // ritorna al form se ci sono errori
    }
	
	//Pannello di controllo, se arriva fino a qui vuol dire che è stato già controllato il ruolo dell'utente
	@GetMapping("/admin")
	public String adminDashboard() {
	    return "admin/indexAdmin";
	}
	
	//Pagina per gestire gli utenti(solo Admin)
	@GetMapping("/admin/users")
    public String manageUsers(Model model) {
        model.addAttribute("credentialsList", this.credentialsService.getAllCredentials());
        return "admin/manageUsers"; // Nome del template HTML che creeremo
    }
	
	//Azione per bannare un utente 
	@PostMapping("/admin/users/{username}/ban")
    public String banUser(@PathVariable("username") String username) {
        this.credentialsService.lockCredentials(username);
        return "redirect:/admin/users";
    }
	
	//Azione per riabilitare un utente 
	@PostMapping("/admin/users/{username}/unban")
    public String unbanUser(@PathVariable("username") String username) {
        this.credentialsService.unlockCredentials(username);
        return "redirect:/admin/users";
    }
}
