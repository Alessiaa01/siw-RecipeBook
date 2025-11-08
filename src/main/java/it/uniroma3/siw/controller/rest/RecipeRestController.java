package it.uniroma3.siw.controller.rest;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.service.RecipeService;

@RestController
public class RecipeRestController {

	  @Autowired
	  private RecipeService recipeService;

	  // Restituisce un singolo film in formato JSON
	  @GetMapping("/rest/recipes/{id}")
	  public Recipe getRecipe(@PathVariable("id") Long id) {
	    return this.recipeService.findById(id);
	  }

	  // Restituisce la lista di tutti i film in formato JSON
	  @GetMapping("/rest/recipes")
	  public List<Recipe> getRecipes() {
	   List<Recipe> recipes= new ArrayList<>();
	   for(Recipe r: this.recipeService.findAll())
	       recipes.add(r);
	   return recipes;
		}
	  }
	


