package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.controller.validator.RecipeValidator;

import jakarta.validation.Valid;

@Controller
public class RecipeController {

	//Questo permette al controller di usare i metodi del service 
	@Autowired
	private RecipeService recipeService;
	
	@Autowired 
	private RecipeValidator recipeValidator;
	  

	
	//Prende una richiesta dal browser, recupera la ricetta dal database e la mostra nella pagina HTML.
	//mostra i dati della ricetta con il codice specificato nell'ultima parte dell'URL 

	@GetMapping("/recipe/{id}")
	public String getRecipe(@PathVariable("id") Long id, Model model) {
		model.addAttribute("recipe", this.recipeService.findById(id));
		return "recipe.html";
	}

	//Quando qualcuno va su /recipes, prende tutte le ricette dal database e le mostra nella pagina recipes.html
	//lista tutte le ricette 
	@GetMapping("/recipes")
	public String getRecipes(Model model) {		
		model.addAttribute("recipes", this.recipeService.findAll());
		return "recipes.html";
	}
	
	//per inserire i dati di una nuova ricetta 
     @GetMapping("/formNewRecipe")
     public String formNewRecipe(Model model) {
    	 model.addAttribute("recipe",new Recipe());
    	 return "formNewRecipe.html";
     }
     
    
     
     @PostMapping("/recipe")
 	public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe, BindingResult bindingResult, Model model) {
 		//controlla se essiste già una ricetta con lo stesso nome
 		this.recipeValidator.validate(recipe, bindingResult);
 		//se non ci sono errori di validazione 
 		if (!bindingResult.hasErrors()) {
 			//salva la ricetta nel database
 			this.recipeService.save(recipe); 
 			//agg. la ricetta salvata al modello
 			model.addAttribute("recipe", recipe);
 			//reindirizza l'utente alla pagina che mostra la nuova ricetta 
 			return "redirect:recipe/"+recipe.getId();
 		} else {
 			//se ci sono errori, torna al form di inserimento mondado i messaggi di errore all'utente 
 			return "formNewRecipe.html"; 
 		}
 	}

}
