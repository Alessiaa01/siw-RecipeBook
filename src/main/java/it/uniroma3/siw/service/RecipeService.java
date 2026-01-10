package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Recipe;

import it.uniroma3.siw.repository.RecipeRepository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

	@Autowired
	private RecipeRepository recipeRepository;

	//ricetta con id 
	@Transactional(readOnly = true)
	public Recipe findById(Long id) {
		return recipeRepository.findById(id).get();
	}
	
	//tutte le ricette 
	@Transactional(readOnly = true)
	public Iterable<Recipe> findAll() {
		return recipeRepository.findAll();
	}
	
	public List<Recipe> findByTitleContainingIgnoreCase(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title);
    }
	
	@Transactional
	public void save(Recipe recipe) {
		recipeRepository.save(recipe);
	}

	public boolean existsByTitle(String title) {
		return recipeRepository.existsByTitle(title);
	}
	

	@Transactional 
	public void deleteById(Long id) {
	    recipeRepository.deleteById(id);
	}
	
	// NUOVO METODO: cerca ricette per nome ingrediente
    @Transactional(readOnly = true)
    public List<Recipe> findByIngredientNameContainingIgnoreCase(String ingredientName) {
        return recipeRepository.findByIngredientsNameContainingIgnoreCase(ingredientName);
    }
    

    public boolean existsByTitleAndIdNot(String title, Long id) {
        return recipeRepository.existsByTitleAndIdNot(title, id);
    }
}
