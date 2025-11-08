package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.repository.RecipeRepository;

@Service
public class RecipeService {

	@Autowired
	private RecipeRepository recipeRepository;

	//ricetta con id 
	public Recipe findById(Long id) {
		return recipeRepository.findById(id).get();
	}
	
	//tutte le ricette 
	public Iterable<Recipe> findAll() {
		return recipeRepository.findAll();
	}
	
	public void save(Recipe recipe) {
		recipeRepository.save(recipe);
	}

	public boolean existsByTitle(String title) {
		return recipeRepository.existsByTitle(title);
	}
}
