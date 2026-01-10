package it.uniroma3.siw.repository;

import org.springframework.data.repository.CrudRepository;
import it.uniroma3.siw.model.Review;
import java.util.List;

public interface ReviewRepository extends CrudRepository<Review, Long> {
    
    // Metodo utile per recuperare tutte le recensioni di una specifica ricetta
    public List<Review> findByRecipeId(Long recipeId);
}