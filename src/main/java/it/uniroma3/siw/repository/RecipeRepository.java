package it.uniroma3.siw.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.model.Recipe;

public interface RecipeRepository extends CrudRepository<Recipe, Long> {
	
	public boolean existsByTitle(String title);	
	
	List<Recipe> findByTitleContainingIgnoreCase(String title);
}