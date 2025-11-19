package it.uniroma3.siw.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.service.RecipeService;

@Component
public class RecipeValidator implements Validator {
    @Autowired
    private RecipeService recipeService;

    @Override
    public void validate(Object o, Errors errors) {
        Recipe recipe = (Recipe)o;
        if (recipe.getTitle() != null 
                && recipeService.existsByTitle(recipe.getTitle())) {
            // MODIFICA QUI:
            // 1. Cambiato "movie.duplicate" in "recipe.duplicate"
            // 2. Aggiunto "Una ricetta con questo nome esiste già" come messaggio di default
            errors.reject("recipe.duplicate", "Una ricetta con questo nome esiste già.");
        }
    }
    
    @Override
    public boolean supports(Class<?> aClass) {
        return Recipe.class.equals(aClass);
    }
}