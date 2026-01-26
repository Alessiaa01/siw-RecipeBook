package it.uniroma3.siw.controller.rest;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.service.RecipeService;

//I suoi metodi restituiscono direttamente l'Oggetto Java (Recipe), che Spring converte automaticamente in JSON.
//Serve per far comunicare il sistema con applicazioni esterne o app mobile.
@RestController
public class RecipeRestController {

	  @Autowired
	  private RecipeService recipeService;

	  // Restituisce una singola ricetta in formato JSON
	  @GetMapping("/rest/recipes/{id}")
	  public Recipe getRecipe(@PathVariable("id") Long id) {
	    return this.recipeService.findById(id);
	  }

	  // Restituisce la lista di tutte le ricette in formato JSON
	  @GetMapping("/rest/recipes")
	  public List<Recipe> getRecipes() {
	   List<Recipe> recipes= new ArrayList<>();
	   for(Recipe r: this.recipeService.findAll())
	       recipes.add(r);
	   return recipes;
		}
	  }
	


