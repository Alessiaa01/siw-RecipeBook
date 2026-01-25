package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.RecipeRepository;
import it.uniroma3.siw.repository.UserRepository;

@Controller 
public class UserController {

    @Autowired 
    private UserRepository userRepository;
    
    @Autowired 
    private RecipeRepository recipeRepository;
    
    @GetMapping("/user/{id}")
    public String getUserProfile(@PathVariable("id") Long id, Model model) {
        // Usa orElse(null) per sicurezza, così non crasha se l'ID è sbagliato
    	
    	User user = userRepository.findById(id).orElse(null);
        
        if (user == null) {
            return "redirect:/recipes"; // Se l'utente non esiste, torna indietro
        }
        
        List<Recipe> ricetteDelloChef = recipeRepository.findByAuthor(user);
        
        model.addAttribute("user", user);
        model.addAttribute("userRecipes", ricetteDelloChef); 
     // Aggiungiamo esplicitamente i preferiti al modello per chiarezza nel template
        model.addAttribute("favoriteRecipes", user.getFavoriteRecipes());
        
        return "userProfile";
    }
}