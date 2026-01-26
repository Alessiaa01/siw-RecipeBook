package it.uniroma3.siw.controller.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.service.RecipeService;

//Serve a impedire che ci siano due ricette con lo stesso identico titolo nel database (Unicità del Titolo)
@Component
public class RecipeValidator implements Validator {

    @Autowired
    private RecipeService recipeService;

    @Override
    public void validate(Object o, Errors errors) {
        Recipe recipe = (Recipe) o;
        String title = recipe.getTitle();

        // 1. Controllo che il titolo non sia nullo o vuoto
        if (title != null && !title.trim().isEmpty()) {

            // 2. CASO MODIFICA (La ricetta ha un ID)
            if (recipe.getId() != null) {
                // Cerchiamo se c'è un'ALTRA ricetta con lo stesso nome ma ID diverso.
                // Se c'è, è un duplicato illegale.
                if (recipeService.existsByTitleAndIdNot(title, recipe.getId())) {
                    // USIAMO rejectValue("title", ...) per mostrare l'errore accanto al campo input
                    errors.rejectValue("title", "duplicate", "Una ricetta con questo nome esiste già.");
                }
            }
            
            // 3. CASO NUOVA RICETTA (La ricetta non ha ID)
            else {
                // Cerchiamo se esiste già una ricetta con questo nome
                if (recipeService.existsByTitle(title)) {
                    errors.rejectValue("title", "duplicate", "Una ricetta con questo nome esiste già.");
                }
            }
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Recipe.class.equals(aClass);
    }
}