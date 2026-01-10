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
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.Credentials;

import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.service.IngredientService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.service.CredentialsService;

import it.uniroma3.siw.controller.validator.RecipeValidator;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;



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
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private CredentialsService credentialsService;

    

    
    //----------UTENTI---------
    // Mostra i dati della ricetta con gli ingredienti e recensioni
    @GetMapping("/recipe/{id}")
    public String getRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredient", new Ingredient()); // necessario per il form admin 
        model.addAttribute("review", new Review()); //aggiunta per la recensione 
        return "recipe.html";
    }

    //Home page e lista di tutte le ricette
    @GetMapping(value = {"/", "/recipes"})
    public String getRecipes(Model model) {		
        model.addAttribute("recipes", this.recipeService.findAll());
        return "recipes.html";
    }
    
    // Mostra la pagina con il form di ricerca
    @GetMapping("/formSearchRecipes")
    public String formSearchRecipes() {
        return "formSearchRecipes.html";
    }
    
     // Gestisce la ricerca per titolo o ingrediente
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

   //----------RECENSIONI(SOLO LOGGATI)-----------

@PostMapping("/recipe/{recipeId}/review")
public String addReview(@PathVariable("recipeId") Long recipeId, // Cambia anche qui il nome della variabile
                        @Valid @ModelAttribute("review") Review review,
                        BindingResult bindingResult, 
                        Model model) {
    
    // Usa la nuova variabile recipeId per cercare la ricetta
    Recipe recipe = recipeService.findById(recipeId);

    if (bindingResult.hasErrors()) {
        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredient", new Ingredient()); 
        return "recipe.html"; 
    }

    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
    if (principal instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) principal;
        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
        
        if (credentials != null && credentials.getUser() != null) {
            review.setUser(credentials.getUser());
            review.setRecipe(recipe);
            reviewService.save(review); 
        }
    }
    
    // Usa recipeId anche per il redirect
    return "redirect:/recipe/" + recipeId;
}

   


    //----------ADMIN----------

@GetMapping("/admin/manageRecipes")
public String manageRecipes(Model model) {
    model.addAttribute("recipes", recipeService.findAll());
    return "admin/manageRecipes.html"; 
}

    // Form per inserire una nuova ricetta
    @GetMapping("/admin/formNewRecipe")
    public String formNewRecipe(Model model) {
        model.addAttribute("recipe", new Recipe());
        return "admin/formNewRecipe.html";
    }
    

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

    
    
    @GetMapping("/admin/recipe/{id}/edit")
    public String editRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredient", new Ingredient());
        return "admin/editRecipe.html";
    }

    @PostMapping("/admin/recipe/{id}/update")
    public String updateRecipe(@PathVariable("id") Long id, 
                               @ModelAttribute("recipe") Recipe updatedRecipe, 
                               BindingResult bindingResult, 
                               Model model) {
        
        // 1. Recupera la ricetta originale dal DB
        Recipe recipeInDb = recipeService.findById(id);
        
        if (recipeInDb == null) {
            return "redirect:/recipes"; // Se l'ID non esiste, torna alla lista
        }

        // 2. TRUCCO: Inseriamo gli ingredienti vecchi nella ricetta che arriva dal form.
        // Questo serve perché il form manda una lista vuota e il validatore potrebbe arrabbiarsi.
        updatedRecipe.setIngredients(recipeInDb.getIngredients());

        // 3. Validazione
        this.recipeValidator.validate(updatedRecipe, bindingResult);

        // 4. CONTROLLO ERRORI (con stampa in console per debug)
        if (bindingResult.hasErrors()) {
            System.out.println("--- ERRORE DI VALIDAZIONE RILEVATO ---");
            bindingResult.getAllErrors().forEach(e -> System.out.println(e.toString()));
            
            // Ricarica la pagina di edit mostrando gli errori
            model.addAttribute("recipe", updatedRecipe);
            model.addAttribute("ingredient", new Ingredient());
            return "admin/editRecipe.html";
        }

        // 5. Se tutto è OK, aggiorniamo i campi manualmente
        recipeInDb.setTitle(updatedRecipe.getTitle());
        recipeInDb.setDescription(updatedRecipe.getDescription());
        recipeInDb.setProcedure(updatedRecipe.getProcedure());
        recipeInDb.setPreparationTime(updatedRecipe.getPreparationTime());
        recipeInDb.setCookingTime(updatedRecipe.getCookingTime());
        recipeInDb.setDifficulty(updatedRecipe.getDifficulty());
        recipeInDb.setServings(updatedRecipe.getServings());
        recipeInDb.setCategory(updatedRecipe.getCategory());
        recipeInDb.setImageUrl(updatedRecipe.getImageUrl());
        
        // Nota: Gli ingredienti NON li tocchiamo qui, perché li gestisci
        // con i tasti "Aggiungi" e "Rimuovi" separati.
        
        // 6. Salvataggio finale nel DB
        recipeService.save(recipeInDb);

        
        
        // OPPURE: Se preferisci tornare all'elenco pubblico usa questa riga invece di quella sopra:
         return "redirect:/recipes";
    }
    

    // Aggiunge un ingrediente direttamente alla ricetta e rimane sulla stessa pagina
    @PostMapping("/admin/recipe/{recipeId}/ingredient/add")
    public String addIngredientToRecipe(@PathVariable("recipeId") Long recipeId,
                                        @ModelAttribute("ingredient") Ingredient ingredient) {
        
        Recipe recipe = recipeService.findById(recipeId);

        // Creiamo un NUOVO ingrediente pulito
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(ingredient.getName());
        newIngredient.setQuantity(ingredient.getQuantity());
        newIngredient.setUnit(ingredient.getUnit());
        newIngredient.setRecipe(recipe); // Colleghiamo alla ricetta

        // Salviamo prima l'ingrediente
        ingredientService.save(newIngredient);

        // Aggiorniamo la ricetta e salviamo
        recipe.getIngredients().add(newIngredient);
        recipeService.save(recipe);

        return "redirect:/admin/recipe/" + recipeId + "/edit";
    }
    
    @PostMapping("/admin/recipe/{recipeId}/ingredient/{ingredientId}/remove")
    public String removeIngredient(@PathVariable Long recipeId, @PathVariable Long ingredientId) {
        // 1. Carica la ricetta
        Recipe recipe = recipeService.findById(recipeId);

        // 2. Rimuovi l'ingrediente cercando SPECIFICATAMENTE il suo ID
        // Questo comando dice: "Togli dalla lista l'elemento che ha questo ID esatto"
        recipe.getIngredients().removeIf(ing -> ing.getId().equals(ingredientId));

        // 3. Salva (l'ingrediente sparirà dal DB grazie a orphanRemoval=true)
        recipeService.save(recipe);

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
    
}
