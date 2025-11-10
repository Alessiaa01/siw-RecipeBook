package it.uniroma3.siw.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.siw.model.Ingredient;
//import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.repository.IngredientRepository;

@Service
public class IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;

   // @Transactional
    public Ingredient save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

   // @Transactional
    public List<Ingredient> findAll() {
    	 return (List<Ingredient>) ingredientRepository.findAll();
    }

   
    
  //  @Transactional
    public Ingredient findById(Long id) {
        return ingredientRepository.findById(id).get();
    }

    //@Transactional
    public boolean existsByName(String name) {
        return ingredientRepository.existsByName(name);
    }
}
