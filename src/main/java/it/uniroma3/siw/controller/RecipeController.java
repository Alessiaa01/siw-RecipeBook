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
import it.uniroma3.siw.model.User;

import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.service.IngredientService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.service.CredentialsService;

import it.uniroma3.siw.controller.validator.RecipeValidator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;



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
    private ReviewService reviewService;


    @Autowired 
    private RecipeValidator recipeValidator;
    
       
    @Autowired
    private CredentialsService credentialsService;

    @InitBinder("recipe") // <--- Specifichiamo che vale solo per l'oggetto "recipe"
    public void initBinder(WebDataBinder binder) {
        // Blocca qualsiasi tentativo di passare l'autore o l'ID dal form
        binder.setDisallowedFields("author", "author.id", "user", "user.id", "id");
    }

    
    //----------UTENTI---------ù
   
    // Mostra i dati della ricetta con gli ingredienti e recensioni
    @GetMapping("/recipe/{id}")
    public String getRecipe(@PathVariable("id") Long id, Model model) {
    	//recuperi la ricetta
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        
        //aggiungi la ricetta e i form vuoti
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
    
    // Mostra la pagina con il form di ricerca(TI DA SOLO IL FORM(FOGLIO BIANCO))
    @GetMapping("/formSearchRecipes")
    public String formSearchRecipes() {
        return "formSearchRecipes.html";
    }
    
    @GetMapping("/searchRecipes")
    public String searchRecipes(@RequestParam(value = "title", required = false) String title, 
                                @RequestParam(value = "ingredient", required = false) String ingredient, 
                                Model model) {
        
        List<Recipe> foundRecipes = new ArrayList<>();

        if (title != null && !title.trim().isEmpty()) {
            foundRecipes = recipeService.findByTitle(title);
        }
        else if (ingredient != null && !ingredient.trim().isEmpty()) {
            foundRecipes = recipeService.findByIngredient(ingredient);
        }
        else {
            foundRecipes = recipeService.findAll();
        }

        model.addAttribute("recipes", foundRecipes);
        
        return "recipes.html"; 
    }

   //----------SOLO LOGGATI)-----------

    @PostMapping("/recipe/{recipeId}/review")
    public String addReview(@PathVariable("recipeId") Long recipeId,
                            @Valid @ModelAttribute("review") Review review,
                            BindingResult bindingResult, 
                            Model model,
                            @ModelAttribute("currentUser") User currentUser) {
        
        // 1. Recuperiamo la ricetta (serve sia per salvarla che per ricaricare la pagina in caso di errore)
        Recipe recipe = recipeService.findById(recipeId);

        // 2. Controllo Errori di Validazione (es. recensione vuota)
        if (bindingResult.hasErrors()) {
            model.addAttribute("recipe", recipe);
            model.addAttribute("ingredient", new Ingredient()); // Necessario perché la vista se lo aspetta
            // Non serve aggiungere currentUser, lo fa il GlobalController
            return "recipe.html"; 
        }

        // 3. Salvataggio
        // Controlliamo che l'utente esista (per sicurezza)
        if (currentUser != null) {
            review.setUser(currentUser);  // Uso l'utente iniettato dal GlobalController!
            review.setRecipe(recipe);
            reviewService.save(review); 
        }
        
        // 4. Post-Redirect-Get Pattern
        return "redirect:/recipe/" + recipeId;
    }

// Form per inserire una nuova ricetta
@GetMapping("/formNewRecipe")
public String formNewRecipe(Model model) {
    model.addAttribute("recipe", new Recipe());
    return "formNewRecipe.html";
}

@PostMapping("/formNewRecipe")
public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe,
                        BindingResult bindingResult, 
                        Model model,
                        @ModelAttribute("currentUser") User currentUser) { // 1. Injection dell'Utente
    
    // 2. Validazione base
    recipeValidator.validate(recipe, bindingResult);
    
    // Se ci sono errori, torniamo al form 
    if (bindingResult.hasErrors()) {
        
        return "formNewRecipe.html"; 
    }
    
    // 3. Gestione e Pulizia Ingredienti
    //  serve a gestire la relazione bidirezionale)
    List<Ingredient> validIngredients = new ArrayList<>();
    
    for (Ingredient ingredient : recipe.getIngredients()) {
        if (ingredient.getName() != null && !ingredient.getName().trim().isEmpty()) {
            ingredient.setRecipe(recipe); // Fondamentale per le chiavi esterne!
            validIngredients.add(ingredient);
        }
    }
    
    recipe.setIngredients(validIngredients); 
    
    // 4. Assegnazione Autore (Semplificata grazie al GlobalController)
    recipe.setAuthor(currentUser);
    
    // 5. Salvataggio
    recipeService.save(recipe);
    
    // 6. Redirect alla pagina della ricetta creata
    return "redirect:/recipe/" + recipe.getId(); 
}
    
    
@GetMapping("/myRecipes")
public String myRecipes(Model model, @ModelAttribute("currentUser") User currentUser) {
    
    // Controllo di sicurezza (se per caso currentUser fosse null)
    if (currentUser == null) {
        // Se non sei loggato, non dovresti essere qui. Ti rimando al login.
        return "redirect:/login";
    }

    // 1. Chiedi al service le ricette di QUESTO utente
    List<Recipe> recipes = recipeService.getRecipesByAuthor(currentUser);
    
    // 2. Mettile nel model
    model.addAttribute("recipes", recipes);
    
    return "myRecipes";
}
   
   
@GetMapping("/recipe/edit/{id}")
public String editRecipe(@PathVariable("id") Long id, 
                         Model model,
                         @ModelAttribute("currentUser") User currentUser) { // 1. User già pronto
    
    Recipe recipe = recipeService.findById(id);
    
    // 2. Controllo Autore: Immediato grazie all'oggetto iniettato
    // (Usa gli ID per sicurezza se non hai implementato equals() in User)
    boolean isAuthor = currentUser != null && recipe.getAuthor().equals(currentUser);

    // 3. Controllo Admin: Lo chiediamo a Spring Security (che lo ha in memoria)
    // Senza fare query al database!
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(Credentials.ADMIN_ROLE)); // o "ADMIN" o "DEFAULT"

    // LOGICA FINALE: Se non sei l'autore E non sei admin -> Via!
    if (!isAuthor && !isAdmin) {
        return "redirect:/recipes?error=notAuthorized";
    }

    model.addAttribute("recipe", recipe);
    model.addAttribute("ingredient", new Ingredient());
    return "editRecipe.html"; 
}
    
@PostMapping("/recipe/update/{id}")
public String updateRecipe(@PathVariable("id") Long id,
                           @ModelAttribute("recipe") Recipe formRecipe,
                           BindingResult bindingResult,
                           Model model,
                           @ModelAttribute("currentUser") User currentUser) {

    // 1. Pulizia sicurezza: stacchiamo l'autore che arriva dal form
    formRecipe.setAuthor(null);

    // 2. Carichiamo la ricetta originale
    Recipe recipeInDb = recipeService.findById(id);
    if (recipeInDb == null) return "redirect:/recipes";

    // 3. Controllo Permessi
    boolean isAuthor = currentUser != null && recipeInDb.getAuthor().getId().equals(currentUser.getId());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(Credentials.ADMIN_ROLE));

    if (!isAuthor && !isAdmin) {
        return "redirect:/recipes?error=notAuthorized";
    }

    // 4. Prepariamo i dati per la validazione
    formRecipe.setAuthor(recipeInDb.getAuthor());
    formRecipe.setIngredients(recipeInDb.getIngredients());

    // 5. Validazione
    this.recipeValidator.validate(formRecipe, bindingResult);
    
    if (bindingResult.hasErrors()) {
        // *** FIX IMPORTANTE PER ERRORE 400 ***
        // Reinseriamo l'ID nell'oggetto form, altrimenti i link nel template
        // diventeranno "/recipe/null"
        formRecipe.setId(id);
        
        model.addAttribute("recipe", formRecipe); 
        model.addAttribute("ingredient", new Ingredient()); 
        return "editRecipe"; 
    }

    // 6. Aggiornamento manuale dei campi
    recipeInDb.setTitle(formRecipe.getTitle());
    recipeInDb.setDescription(formRecipe.getDescription());
    recipeInDb.setCategory(formRecipe.getCategory());
    recipeInDb.setPreparationTime(formRecipe.getPreparationTime());
    recipeInDb.setCookingTime(formRecipe.getCookingTime());
    recipeInDb.setDifficulty(formRecipe.getDifficulty());
    recipeInDb.setServings(formRecipe.getServings());
    recipeInDb.setProcedure(formRecipe.getProcedure());
    recipeInDb.setImageUrl(formRecipe.getImageUrl());
    recipeInDb.setTags(formRecipe.getTags());

    // Salviamo (senza toccare l'autore)
    recipeService.save(recipeInDb);

    return "redirect:/recipe/" + id;
}

    @PostMapping("/recipe/{recipeId}/ingredient/add")
    public String addIngredientToRecipe(@PathVariable("recipeId") Long recipeId,
                                        @ModelAttribute("ingredient") Ingredient ingredient) {
        
        Recipe recipe = recipeService.findById(recipeId);
        
        // (Qui potresti aggiungere lo stesso controllo permessi se vuoi essere sicurissima)

        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(ingredient.getName());
        newIngredient.setQuantity(ingredient.getQuantity());
        newIngredient.setUnit(ingredient.getUnit());
        newIngredient.setRecipe(recipe);

        ingredientService.save(newIngredient);
        recipe.getIngredients().add(newIngredient);
        recipeService.save(recipe);

        return "redirect:/recipe/edit/" + recipeId; // Ricarica la pagina di modifica
    }
  /*
    // Aggiunge un ingrediente direttamente alla ricetta e rimane sulla stessa pagina
    @PostMapping("/recipe/{recipeId}/ingredient/add")
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
    */
    
    @GetMapping("/recipe/{recipeId}/ingredient/{ingredientId}/remove") // Cambiato in GET per comodità nei link
    public String removeIngredient(@PathVariable Long recipeId, @PathVariable Long ingredientId) {
        Recipe recipe = recipeService.findById(recipeId);
        
        // Rimuovi l'ingrediente
        recipe.getIngredients().removeIf(ing -> ing.getId().equals(ingredientId));
        
        recipeService.save(recipe);

        return "redirect:/recipe/edit/" + recipeId;
    }
    /*
    @PostMapping("/recipe/{recipeId}/ingredient/{ingredientId}/remove")
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
    */
    
 // --- CANCELLA RICETTA (POST) ---
  
    @PostMapping("/recipe/delete/{id}") 
    public String deleteRecipe(@PathVariable("id") Long id) {
        
        Recipe recipe = recipeService.findById(id);
        
        // RECUPERO UTENTE CORRENTE
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Credentials credentials = credentialsService.getCredentials(authentication.getName());

        // CONTROLLO DI SICUREZZA
        if (recipe != null && (recipe.getAuthor().equals(credentials.getUser()) || 
            credentials.getRole().equals(Credentials.ADMIN_ROLE))) {
            
            recipeService.deleteById(id);
            return "redirect:/myRecipes"; 
            
        } else {
            return "redirect:/recipe/" + id + "?error=notAuthorized";
        }
    }
    
    /*
    @PostMapping("/recipe/{id}/delete") // Mappa l'URL e il metodo POST corretto
    public String deleteRecipe(@PathVariable("id") Long id) {
        
        // 1. Esegue la cancellazione del record
        recipeService.deleteById(id);
        
        // 2. Reindirizza l'amministratore alla lista di gestione aggiornata
        // (Assumendo che la tua pagina di gestione sia mappata a /admin)
        return "redirect:/admin/manageRecipes";
    }  
    */
  //----------ADMIN----------

    @GetMapping("/admin/manageRecipes")
    public String manageRecipes(Model model) {
        model.addAttribute("recipes", recipeService.findAll());
        return "admin/manageRecipes.html"; 
    }   
 // Metodo di supporto per recuperare lo username corretto (Email o User classico)
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return oauth2User.getAttribute("email");
        }
        return authentication.getName();
    }
    
    
    
}
