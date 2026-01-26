package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;


@Controller
public class AuthenticationController {
	
	@Autowired
	private CredentialsService credentialsService; //gestisce username, psw e ruoli
 
    @Autowired
	private UserService userService;//salva i dati anagrafici 
    
    @Autowired
    private RecipeService recipeService;//serve all'admin per vedere l'elenco delle ricette
	
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

	@GetMapping(value = "/success")
	public String defaultAfterLogin() {
	    // tutti vengono mandati alle ricette
	    return "welcome";
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
            userService.saveUser(user);//salva dati anagrafici
            credentials.setUser(user);//collega credenziali all'utente
            credentialsService.saveCredentials(credentials); //salva le credenziali 
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
	@GetMapping("/admin/manageUsers")
    public String manageUsers(Model model) {
        model.addAttribute("credentialsList", this.credentialsService.getAllCredentials());
        return "admin/manageUsers"; 
    }
	
	@GetMapping("/admin/manageRecipes")
	public String manageRecipes(Model model) {
	    // Carica le ricette dal database
	    model.addAttribute("recipes", this.recipeService.findAll()); 
	    
	    return "admin/manageRecipes"; 
	}
	
	//Azione per bannare un utente 
	@PostMapping("/admin/manageUsers/{username}/ban")
    public String banUser(@PathVariable("username") String username) {
        this.credentialsService.lockCredentials(username);
        return "redirect:/admin/manageUsers";
    }
	
	//Azione per riabilitare un utente 
	@PostMapping("/admin/manageUsers/{username}/unban")
    public String unbanUser(@PathVariable("username") String username) {
        this.credentialsService.unlockCredentials(username);
        return "redirect:/admin/manageUsers";
    }
}
