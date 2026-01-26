package it.uniroma3.siw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.uniroma3.siw.model.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    public boolean existsByName(String name);
}
