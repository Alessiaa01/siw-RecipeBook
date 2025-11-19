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
//import java.util.Set;
import java.util.ArrayList;
//import java.util.HashSet;
import jakarta.validation.Valid;

@Controller
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired 
    private RecipeValidator recipeValidator;

    
    
    //----------UTENTI---------
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
    
    // Mostra la pagina con il form di ricerca
    @GetMapping("/formSearchRecipes")
    public String formSearchRecipes() {
        return "formSearchRecipes.html";
    }
    
 // Nel file it.uniroma3.siw.controller.RecipeController.java

 // ... (assicurati che esista l'import: import java.util.ArrayList;) ...
 // ... (e l'import: import org.springframework.web.bind.annotation.RequestParam;) ...

     // MODIFICATO: Gestisce il submit del form e mostra i risultati per titolo o ingrediente
     @GetMapping("/searchRecipes")
     public String searchRecipes(Model model, 
                                 @RequestParam(value = "title", required = false) String title, 
                                 @RequestParam(value = "ingredient", required = false) String ingredientName) {
     	
     	 List<Recipe> foundRecipes = new ArrayList<>();

         if (title != null && !title.trim().isEmpty()) {
             // Ricerca per Titolo
             foundRecipes = recipeService.findByTitleContainingIgnoreCase(title);
             model.addAttribute("searchTerm", title);
             model.addAttribute("searchType", "Titolo");
         } else if (ingredientName != null && !ingredientName.trim().isEmpty()) {
              // NUOVA Ricerca per Ingrediente
             foundRecipes = recipeService.findByIngredientNameContainingIgnoreCase(ingredientName);
             model.addAttribute("searchTerm", ingredientName);
             model.addAttribute("searchType", "Ingrediente");
         }
         // Se entrambi sono vuoti, foundRecipes è vuoto.

         model.addAttribute("recipes", foundRecipes);
         return "foundRecipes.html"; // Ritorna la pagina dei risultati
     }
/*
    // Gestisce il submit del form e mostra i risultati
    @GetMapping("/searchRecipes")
    public String searchRecipes(Model model, @RequestParam String title) {
    	 List<Recipe> foundRecipes = recipeService.findByTitleContainingIgnoreCase(title);
        model.addAttribute("recipes", foundRecipes);
        return "foundRecipes.html";
    }

  */  
    //----------ADMIN----------
    // Form per inserire una nuova ricetta
    @GetMapping("/admin/formNewRecipe")
    public String formNewRecipe(Model model) {
        model.addAttribute("recipe", new Recipe());
        return "admin/formNewRecipe.html";
    }
    
 // Nel file it.uniroma3.siw.controller.RecipeController.java

    @PostMapping("/admin/formNewRecipe")
    public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe,
                            BindingResult bindingResult, Model model) {
        
        // 1. Validazione base (Titolo, Descrizione, DDL Auto)
        recipeValidator.validate(recipe, bindingResult);
        
        // CONTROLLO ESSENZIALE: Se ci sono errori, torna immediatamente al form!
        if (bindingResult.hasErrors()) {
            return "admin/formNewRecipe.html"; 
        }
        
        // 2. LOGICA INGREDIENTI: Viene eseguita SOLO se la validazione base è OK.
        List<Ingredient> validIngredients = new ArrayList<>();
        
        // ITERAZIONE CRUCIALE: Controlla gli ingredienti che Spring ha mappato dal form
        for (Ingredient ingredient : recipe.getIngredients()) {
            
            // Filtra: Salviamo solo se il campo Nome non è vuoto
            if (ingredient.getName() != null && !ingredient.getName().trim().isEmpty()) {
                
                // ASSOCIAZIONE ESSENZIALE: Imposta il riferimento bidirezionale
                ingredient.setRecipe(recipe); 
                
                validIngredients.add(ingredient);
            }
        }
        
        // Sostituisce la lista originale con solo gli elementi validi
        recipe.setIngredients(validIngredients); 
        
        // 3. Salvataggio
        recipeService.save(recipe);
        
        // Reindirizza alla pagina di visualizzazione pubblica della ricetta appena creata
        return "redirect:/recipe/" + recipe.getId(); 
    }

    
    
    // Salvataggio nuova ricetta
   
  /*
    @PostMapping("/admin/formNewRecipe")
    public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe,
                            BindingResult bindingResult, Model model) {
        
        // ... (Validazione e gestione errori standard) ...
        
        List<Ingredient> validIngredients = new ArrayList<>();
        
        // ITERAZIONE CRUCIALE: Controlla gli ingredienti che Spring ha mappato dal form
        for (Ingredient ingredient : recipe.getIngredients()) {
            
            // Filtra: Salviamo solo se il campo Nome non è vuoto
            if (ingredient.getName() != null && !ingredient.getName().trim().isEmpty()) {
                
                // ASSOCIAZIONE ESSENZIALE: Imposta il riferimento bidirezionale a Recipe
                ingredient.setRecipe(recipe); 
                
                validIngredients.add(ingredient);
            }
        }
        
        // Sostituisce la lista originale con solo gli elementi validi
        recipe.setIngredients(validIngredients); 
        
        // Salvataggio (JPA salva anche gli ingredienti validi grazie a CascadeType.ALL)
        recipeService.save(recipe);
        
        // Reindirizza alla pagina di visualizzazione pubblica della ricetta appena creata
        return "redirect:/recipe/" + recipe.getId(); 
    }
    
 

    @PostMapping("/admin/formNewRecipe")
    public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe,
                            BindingResult bindingResult, Model model) {
        
        // ... (Validazione e gestione errori restano invariati) ...
        
        recipeValidator.validate(recipe, bindingResult);
        if (bindingResult.hasErrors()) {
            return "admin/formNewRecipe"; 
        }
        
        // 1. Salvataggio della ricetta base
        recipeService.save(recipe);
        
        // 2. NUOVO REINDIRIZZAMENTO: Vai alla pagina di visualizzazione pubblica singola.
        // L'Admin vedrà la ricetta finita e, se necessario, potrà cliccare sul link "Modifica" 
        // che porta a /admin/recipe/{id}/edit.
        return "redirect:/recipe/" + recipe.getId(); 
    }
  */  
    @GetMapping("/admin/recipe/{id}/edit")
    public String editRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredient", new Ingredient());
        return "admin/editRecipe.html";
    }

    @PostMapping("/admin/recipe/{id}/update")
    public String updateRecipe(@PathVariable("id") Long id, @ModelAttribute("recipe") Recipe updatedRecipe) {
        Recipe recipe = recipeService.findById(id);
        if (recipe != null) {
        	
        	
            recipe.setImageUrl(updatedRecipe.getImageUrl());
            recipe.setTitle(updatedRecipe.getTitle());
            recipe.setDescription(updatedRecipe.getDescription());
            recipe.setPreparationTime(updatedRecipe.getPreparationTime());
            recipe.setCookingTime(updatedRecipe.getCookingTime()); 
            recipe.setDifficulty(updatedRecipe.getDifficulty());
            recipe.setServings(updatedRecipe.getServings());
            recipe.setProcedure(updatedRecipe.getProcedure());
            recipe.setCategory(updatedRecipe.getCategory());
            recipe.setTags(updatedRecipe.getTags());
            recipeService.save(recipe);
        }
        return "redirect:/admin/recipe/" + id + "/edit";
    }


    // Aggiunge un ingrediente direttamente alla ricetta e rimane sulla stessa pagina
    @PostMapping("/admin/recipe/{id}/ingredient/add")
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

        return "redirect:/admin/recipe/" + id + "/edit"; // rimani sulla stessa pagina della ricetta
    }
    
    @PostMapping("/admin/recipe/{recipeId}/ingredient/{ingredientId}/remove")
    public String removeIngredient(@PathVariable Long recipeId, @PathVariable Long ingredientId) {
        Recipe recipe = recipeService.findById(recipeId);
        Ingredient ingredient = ingredientService.findById(ingredientId);

        if (recipe != null && ingredient != null) {
            recipe.getIngredients().remove(ingredient); // rimuove dalla lista
            recipeService.save(recipe); // salva la ricetta aggiornata
        }

        // orphanRemoval farà eliminare automaticamente l’ingrediente dal DB
        return "redirect:/admin/recipe/" + recipeId + "/edit";
    }
    
    @PostMapping("/admin/recipe/{id}/delete") // Mappa l'URL e il metodo POST corretto
    public String deleteRecipe(@PathVariable("id") Long id) {
        
        // 1. Esegue la cancellazione del record
        recipeService.deleteById(id);
        
        // 2. Reindirizza l'amministratore alla lista di gestione aggiornata
        // (Assumendo che la tua pagina di gestione sia mappata a /admin)
        return "redirect:/admin/manageRecipes";
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
    // AGGIUNGI QUESTO (NECESSARIO PER LA LISTA ADMIN CON I BOTTONI DELETE)
@GetMapping("/admin/manageRecipes")
public String manageRecipes(Model model) {
    model.addAttribute("recipes", recipeService.findAll());
    return "admin/manageRecipes.html"; 
}



   

   
   

   

   
    
}
