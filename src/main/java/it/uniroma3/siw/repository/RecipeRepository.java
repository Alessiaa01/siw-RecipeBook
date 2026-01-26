package it.uniroma3.siw.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
	
	public boolean existsByTitle(String title);	
	
	List<Recipe> findByTitleContainingIgnoreCase(String title);
	
	// NUOVO METODO: Trova ricette dove l'ingrediente contiene la stringa (case-insensitive)
    List<Recipe> findByIngredientsNameContainingIgnoreCase(String ingredientName);
    
    
    public boolean existsByTitleAndIdNot(String title, Long id);
    
    public List<Recipe> findByAuthor(User author);
}