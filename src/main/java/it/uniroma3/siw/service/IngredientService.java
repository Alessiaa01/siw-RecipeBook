package it.uniroma3.siw.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.model.Ingredient;
//import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.repository.IngredientRepository;

@Service
public class IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;

    //---STANDARD---
   @Transactional
    public Ingredient save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }
   @Transactional
   public void deleteById(Long id) {
       ingredientRepository.deleteById(id);
   }
   @Transactional
    public void delete(Ingredient ingredient) {
        ingredientRepository.delete(ingredient);
    }

   //---Solo lettura ---
   
   @Transactional(readOnly = true)
    public List<Ingredient> findAll() {
    	 return this.ingredientRepository.findAll();
    }

  
   @Transactional(readOnly = true)
    public Ingredient findById(Long id) {
        return ingredientRepository.findById(id).orElse(null);
    }
    
   @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return ingredientRepository.existsByName(name);
    }
}
