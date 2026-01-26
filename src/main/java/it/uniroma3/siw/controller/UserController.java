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
import it.uniroma3.siw.service.UserService;
import it.uniroma3.siw.service.RecipeService;

@Controller 
public class UserController {

    @Autowired 
    private UserService userService;
    
    @Autowired 
    private RecipeService recipeService;
    
    //gestisce i profili pubbloco degli utenti 
    @GetMapping("/user/{id}")
    public String getUserProfile(@PathVariable("id") Long id, Model model) {
    	
    	User user = userService.findById(id);
        
        if (user == null) {
            return "redirect:/recipes"; // Se l'utente non esiste, torna indietro
        }
        
    	//recuperiamo dal DB tutte le ricette scritte da questo utente
        List<Recipe> ricetteDelloChef = recipeService.findByAuthor(user);
        
        model.addAttribute("user", user);
        model.addAttribute("userRecipes", ricetteDelloChef); 
        
     // Aggiungiamo esplicitamente i preferiti al modello 
        model.addAttribute("favoriteRecipes", user.getFavoriteRecipes());
        
        return "userProfile";
    }
}