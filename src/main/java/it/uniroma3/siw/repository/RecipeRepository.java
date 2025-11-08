package it.uniroma3.siw.repository;


import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.model.Recipe;

public interface RecipeRepository extends CrudRepository<Recipe, Long> {
	
	public boolean existsByTitle(String title);	
}