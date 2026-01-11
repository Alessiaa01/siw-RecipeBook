package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;

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
	public List<Recipe> findAll() {
	    // Il cast (List<Recipe>) trasforma l'Iterable del database in una Lista vera e propria
	    return (List<Recipe>) recipeRepository.findAll();
	}
	
	
	
	@Transactional
	public void save(Recipe recipe) {
		recipeRepository.save(recipe);
	}

	public boolean existsByTitle(String title) {
		return recipeRepository.existsByTitle(title);
	}
	// METODI PER LA RICERCA
    public List<Recipe> findByTitle(String title) {
        return recipeRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Recipe> findByIngredient(String ingredientName) {
        return recipeRepository.findByIngredientsNameContainingIgnoreCase(ingredientName);
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
    
 // Aggiungi questo metodo nella classe RecipeService
    public List<Recipe> getRecipesByAuthor(User author) {
        return recipeRepository.findByAuthor(author);
    }
}
