package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.service.IngredientService;
import it.uniroma3.siw.controller.validator.RecipeValidator;

import java.util.List;

import jakarta.validation.Valid;

@Controller
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired 
    private RecipeValidator recipeValidator;

    // Mostra i dati della ricetta con gli ingredienti
    @GetMapping("/recipe/{id}")
    public String getRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredient", new Ingredient()); // necessario per il form
        return "recipe.html";
    }

    // Lista tutte le ricette
    @GetMapping("/recipes")
    public String getRecipes(Model model) {		
        model.addAttribute("recipes", this.recipeService.findAll());
        return "recipes.html";
    }

    // Form per inserire una nuova ricetta
    @GetMapping("/formNewRecipe")
    public String formNewRecipe(Model model) {
        model.addAttribute("recipe", new Recipe());
        return "formNewRecipe.html";
    }

    // Salvataggio nuova ricetta
    @PostMapping("/recipe")
    public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe, BindingResult bindingResult, Model model) {
        this.recipeValidator.validate(recipe, bindingResult);
        if (!bindingResult.hasErrors()) {
            this.recipeService.save(recipe);
            return "redirect:/recipe/" + recipe.getId();
        } else {
            return "formNewRecipe.html"; 
        }
    }

    // Aggiunge un ingrediente direttamente alla ricetta e rimane sulla stessa pagina
    @PostMapping("/recipe/{id}")
    public String addIngredientToRecipe(@PathVariable("id") Long id,
                                        @ModelAttribute("ingredient") Ingredient ingredient) {
        Recipe recipe = recipeService.findById(id);

        // Crea sempre un nuovo ingrediente
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(ingredient.getName());
        newIngredient.setQuantity(ingredient.getQuantity());
        newIngredient.setUnit(ingredient.getUnit());
        newIngredient.setRecipe(recipe);

        ingredientService.save(newIngredient);

        recipe.getIngredients().add(newIngredient);
        recipeService.save(recipe);

        return "redirect:/recipe/" + id; // rimani sulla stessa pagina della ricetta
    }
  /*  
    @PostMapping("/recipe/{recipeId}/ingredient/{ingredientId}/delete")
    public String deleteIngredient(@PathVariable("recipeId") Long recipeId,
                                   @PathVariable("ingredientId") Long ingredientId) {
        Recipe recipe = recipeService.findById(recipeId);
        Ingredient ingredient = ingredientService.findById(ingredientId);

        if (recipe != null && ingredient != null) {
            recipe.getIngredients().remove(ingredient); // rimuove dalla lista della ricetta
            recipeService.save(recipe); // aggiorna la ricetta
            ingredientService.delete(ingredient); // elimina l’ingrediente dal DB
        }

        return "redirect:/recipe/" + recipeId;
    }
*/
    @GetMapping("/recipe/{id}/edit")
    public String editRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredient", new Ingredient());
        return "editRecipe.html";
    }

    @PostMapping("/recipe/{id}/update")
    public String updateRecipe(@PathVariable("id") Long id, @ModelAttribute("recipe") Recipe updatedRecipe) {
        Recipe recipe = recipeService.findById(id);

        if (recipe != null) {
            recipe.setTitle(updatedRecipe.getTitle());
            recipe.setDescription(updatedRecipe.getDescription());
            recipe.setPreparationTime(updatedRecipe.getPreparationTime());
            recipe.setDifficulty(updatedRecipe.getDifficulty());
            recipe.setServings(updatedRecipe.getServings());
            recipe.setProcedure(updatedRecipe.getProcedure());
            recipe.setCategory(updatedRecipe.getCategory());
            recipe.setTags(updatedRecipe.getTags());

            recipeService.save(recipe);
        }

        return "redirect:/recipe/" + id;
    }

   

    @PostMapping("/recipe/{recipeId}/ingredient/{ingredientId}/remove")
    public String removeIngredient(@PathVariable Long recipeId, @PathVariable Long ingredientId) {
        Recipe recipe = recipeService.findById(recipeId);
        Ingredient ingredient = ingredientService.findById(ingredientId);

        if (recipe != null && ingredient != null) {
            recipe.getIngredients().remove(ingredient); // rimuove dalla lista
            recipeService.save(recipe); // salva la ricetta aggiornata
        }

        // orphanRemoval farà eliminare automaticamente l’ingrediente dal DB
        return "redirect:/recipe/" + recipeId + "/edit";
    }

    // Mostra la pagina con il form di ricerca
    @GetMapping("/formSearchRecipes")
    public String formSearchRecipes() {
        return "formSearchRecipes.html";
    }

    // Gestisce il submit del form e mostra i risultati
    @GetMapping("/searchRecipes")
    public String searchRecipes(Model model, @RequestParam String title) {
    	 List<Recipe> foundRecipes = recipeService.findByTitleContainingIgnoreCase(title);
        model.addAttribute("recipes", foundRecipes);
        return "foundRecipes.html";
    }
    
}
