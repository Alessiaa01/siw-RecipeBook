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

	// RICERCA PER ID
	
	//---OPERAZIONI STANDARD---
	@Transactional(readOnly = true)
	public Recipe findById(Long id) {
		return recipeRepository.findById(id).orElse(null);
	}
	
	// TUTTE LE RICETTE
	@Transactional(readOnly = true)
	public List<Recipe> findAll() {
	    return this.recipeRepository.findAll();
	}
	
	// SALVATAGGIO
		@Transactional
		public void save(Recipe recipe) {
			recipeRepository.save(recipe);
		}
		
		//CANCELLAZIONE
		@Transactional 
		public void deleteById(Long id) {
		    recipeRepository.deleteById(id);
		}

	
	//---METODI DI RICERCA---
	  @Transactional(readOnly = true)
	    public List<Recipe> findByAuthor(User author) {
	        return recipeRepository.findByAuthor(author);
	    }
	
	// METODI PER LA RICERCA E VALIDAZIONE
		@Transactional(readOnly = true)
	    public List<Recipe> findByTitle(String title) {
	        return recipeRepository.findByTitleContainingIgnoreCase(title);
	    }

		@Transactional(readOnly = true)
	    public List<Recipe> findByIngredient(String ingredientName) {
	        return recipeRepository.findByIngredientsNameContainingIgnoreCase(ingredientName);
	    }
		
	
	//---VALIDAZIONE---
		@Transactional(readOnly = true)
	public boolean existsByTitle(String title) {
		return recipeRepository.existsByTitle(title);
	}
		@Transactional(readOnly = true)
    public boolean existsByTitleAndIdNot(String title, Long id) {
        return recipeRepository.existsByTitleAndIdNot(title, id);
    }
   
    
 
}
