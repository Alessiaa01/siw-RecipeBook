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
        Recipe recipe = (Recipe) o;
        String title = recipe.getTitle();

        // 1. Controllo che il titolo non sia nullo, vuoto o fatto solo di spazi
        if (title != null && !title.trim().isEmpty()) {

            // 2. SE LA RICETTA HA UN ID (Significa che è una MODIFICA)
            if (recipe.getId() != null) {
                // Controlliamo se esiste un'ALTRA ricetta con lo stesso nome (ma ID diverso)
                if (recipeService.existsByTitleAndIdNot(title, recipe.getId())) {
                    errors.reject("recipe.duplicate", "Una ricetta con questo nome esiste già.");
                }
            }
            
            // 3. SE LA RICETTA NON HA ID (Significa che è NUOVA)
            else {
                // Controlliamo semplicemente se il titolo esiste già
                if (recipeService.existsByTitle(title)) {
                    errors.reject("recipe.duplicate", "Una ricetta con questo nome esiste già.");
                }
            }
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Recipe.class.equals(aClass);
    }
}