package it.uniroma3.siw.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.User;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
	
	//serve per impedire i duplicati
	public boolean existsByTitle(String title);	
	
	List<Recipe> findByTitleContainingIgnoreCase(String title);
	
	// Trova ricette dove l'ingrediente contiene la stringa (case-insensitive)
    List<Recipe> findByIngredientsNameContainingIgnoreCase(String ingredientName);
    
    //se esiste un titolo di un'altra ricetta che non sia la mia
    public boolean existsByTitleAndIdNot(String title, Long id);
    
    //lista filtrata per le mie ricette o il profilo 
    public List<Recipe> findByAuthor(User author);
}