package it.uniroma3.siw.controller;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import it.uniroma3.siw.controller.validator.RecipeValidator;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Ingredient;
import it.uniroma3.siw.model.Recipe;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.IngredientService;
import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.service.ReviewService;
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

    // Blocca l'invio malevolo di campi sensibili tramite form
    @InitBinder("recipe")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("author", "author.id", "user", "user.id", "id");
    }

    // -------------------------------------------------------------------------
    // SEZIONE PUBBLICA (Accessibile a tutti)
    // -------------------------------------------------------------------------

    @GetMapping("/recipe/{id}")
    public String getRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id);
        model.addAttribute("recipe", recipe);
        
        // Oggetti vuoti per i form nella pagina (recensioni e ingredienti admin)
        model.addAttribute("ingredient", new Ingredient()); 
        model.addAttribute("review", new Review()); 
        
        return "recipe.html";
    }

    @GetMapping(value = {"/", "/recipes"})
    public String getRecipes(Model model) {     
        model.addAttribute("recipes", this.recipeService.findAll());
        return "recipes.html";
    }
    
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

    // -------------------------------------------------------------------------
    // SEZIONE PROTETTA (Richiede Login)
    // -------------------------------------------------------------------------

    @PostMapping("/recipe/{recipeId}/review")
    public String addReview(@PathVariable("recipeId") Long recipeId,
                            @Valid @ModelAttribute("review") Review review,
                            BindingResult bindingResult, 
                            Model model) {
        
        User currentUser = (User) model.getAttribute("currentUser");
        Recipe recipe = recipeService.findById(recipeId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("recipe", recipe);
            model.addAttribute("ingredient", new Ingredient()); 
            return "recipe.html"; 
        }

        if (currentUser != null) {
            review.setUser(currentUser); 
            review.setRecipe(recipe);
            reviewService.save(review); 
        }
        
        return "redirect:/recipe/" + recipeId;
    }

    @GetMapping("/formNewRecipe")
    public String formNewRecipe(Model model) {
    	Recipe recipe = new Recipe();
    	// IMPOSTO IL DEFAULT A OGGI
    	recipe.setCreationDate(LocalDate.now());
        model.addAttribute("recipe", new Recipe());
        return "formNewRecipe.html";
    }

    @PostMapping("/formNewRecipe")
    public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe,
                            BindingResult bindingResult, 
                            Model model) {
        
        User currentUser = (User) model.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        recipeValidator.validate(recipe, bindingResult);
        if (bindingResult.hasErrors()) {
            return "formNewRecipe.html"; 
        }
        
        recipe.setAuthor(currentUser);
        recipeService.save(recipe);
        
        return "redirect:/recipe/" + recipe.getId(); 
    }
    
    @GetMapping("/myRecipes")
    public String myRecipes(Model model) {
        User currentUser = (User) model.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Recipe> recipes = recipeService.getRecipesByAuthor(currentUser);
        model.addAttribute("recipes", recipes);
        
        return "myRecipes.html"; 
    }
   
    // -------------------------------------------------------------------------
    // SEZIONE MODIFICA (Richiede Autore o Admin)
    // -------------------------------------------------------------------------

    @GetMapping("/recipe/edit/{id}")
    public String editRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id); //prende in archivio la ricetta
        User currentUser = (User) model.getAttribute("currentUser");

        if (!isAuthorized(recipe, currentUser)) {
            return "redirect:/recipes?error=notAuthorized";
        }

        model.addAttribute("recipe", recipe); //vecchia ricetta da modificare 
        model.addAttribute("ingredient", new Ingredient());
        return "editRecipe.html"; 
    }
    
    @PostMapping("/recipe/update/{id}")
    public String updateRecipe(@PathVariable("id") Long id,
                               @ModelAttribute("recipe") Recipe formRecipe,
                               BindingResult bindingResult,
                               Model model) {

        User currentUser = (User) model.getAttribute("currentUser");
        Recipe recipeInDb = recipeService.findById(id); //se siste, recuperi la ricetta originale dal database(recipeInDb)
        
        if (recipeInDb == null) 
        	return "redirect:/recipes";

        if (!isAuthorized(recipeInDb, currentUser)) {
            return "redirect:/recipes?error=notAuthorized";
        }

        // Prepariamo l'oggetto per la validazione
        //metto l'ID così capisce che è la ricetta vecchia, e non un duplicato
        formRecipe.setId(id);
        //metto gli ingredienti vecchi
        formRecipe.setIngredients(recipeInDb.getIngredients()); // Manteniamo gli ingredienti esistenti
        //controlla se ho fatto errori
        this.recipeValidator.validate(formRecipe, bindingResult);
        
        if (bindingResult.hasErrors()) {
            // Trick per visualizzare l'autore nel template anche in caso di errore
        	//creo una copia temporanea dell autore 
            User dummyAuthor = new User();
            
            //copio i dati dall'autore vero che ho recuperato dal DB(recipeInDb)
            dummyAuthor.setId(recipeInDb.getAuthor().getId());
            dummyAuthor.setName(recipeInDb.getAuthor().getName());
            dummyAuthor.setSurname(recipeInDb.getAuthor().getSurname());
            //assegni l'oggetto dummyAuthor al campo author dell'oggetto formRecipe
            formRecipe.setAuthor(dummyAuthor);
            
            model.addAttribute("recipe", formRecipe); //contiene i dati che l'utente ha appena inserito nel form(anche quelli sbagliati)
            model.addAttribute("ingredient", new Ingredient());//per non dare errore
            return "editRecipe.html"; 
        }

        // Aggiornamento manuale dei campi
        recipeInDb.setTitle(formRecipe.getTitle());
        recipeInDb.setDescription(formRecipe.getDescription());
        recipeInDb.setCategory(formRecipe.getCategory());
        recipeInDb.setPreparationTime(formRecipe.getPreparationTime());
        recipeInDb.setCookingTime(formRecipe.getCookingTime());
        recipeInDb.setDifficulty(formRecipe.getDifficulty());
        recipeInDb.setServings(formRecipe.getServings());
        recipeInDb.setProcedure(formRecipe.getProcedure());
        recipeInDb.setImageUrl(formRecipe.getImageUrl());
        recipeInDb.setTags(formRecipe.getTags()); // Scommenta se gestisci i tag

        recipeService.save(recipeInDb);

        return "redirect:/recipe/" + id;
    }

    @PostMapping("/recipe/{recipeId}/ingredient/add")
    public String addIngredientToRecipe(@PathVariable("recipeId") Long recipeId,
                                        @ModelAttribute("ingredient") Ingredient ingredient,
                                        Model model) {
        
        User currentUser = (User) model.getAttribute("currentUser");
        Recipe recipe = recipeService.findById(recipeId);
        
        if (recipe == null) 
        	return "redirect:/recipes";

        if (!isAuthorized(recipe, currentUser)) {
            return "redirect:/recipe/" + recipeId + "?error=notAuthorized";
        }

        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(ingredient.getName());
        newIngredient.setQuantity(ingredient.getQuantity());
        newIngredient.setUnit(ingredient.getUnit());
        
        //collega l'ingrediente alla ricetta nel database: questo ingrediente appartiene a questa ricetta specifica
         newIngredient.setRecipe(recipe);

         //il salvataggio avviene in due step:
         
        this.ingredientService.save(newIngredient);
        
        recipe.getIngredients().add(newIngredient);
        this.recipeService.save(recipe);

        return "redirect:/recipe/edit/" + recipeId;
    }
     
    // MODIFICATO IN POST PER SICUREZZA
    @PostMapping("/recipe/{recipeId}/ingredient/{ingredientId}/remove")
    public String removeIngredient(@PathVariable Long recipeId, 
                                   @PathVariable Long ingredientId,
                                   Model model) {
        
        User currentUser = (User) model.getAttribute("currentUser");
        Recipe recipe = recipeService.findById(recipeId);
        
        if (recipe == null) return "redirect:/recipes";

        if (!isAuthorized(recipe, currentUser)) {
            return "redirect:/recipe/" + recipeId + "?error=notAuthorized";
        }

        recipe.getIngredients().removeIf(ing -> ing.getId().equals(ingredientId));
        recipeService.save(recipe);

        return "redirect:/recipe/edit/" + recipeId;
    }    
    
    @PostMapping("/recipe/delete/{id}") 
    public String deleteRecipe(@PathVariable("id") Long id, Model model) {
        
        Recipe recipe = recipeService.findById(id);
        User currentUser = (User) model.getAttribute("currentUser");

        if (recipe == null) 
        	return "redirect:/recipes";

        if (isAuthorized(recipe, currentUser)) {
            recipeService.deleteById(id);
            return "redirect:/myRecipes"; 
        } else {
            return "redirect:/recipe/" + id + "?error=notAuthorized";
        }
    }
    
    // -------------------------------------------------------------------------
    // SEZIONE ADMIN (Generale)
    // -------------------------------------------------------------------------

    @GetMapping("/admin/manageRecipes")
    public String manageRecipes(Model model) {
        model.addAttribute("recipes", recipeService.findAll());
        return "admin/manageRecipes.html"; 
    }

    // -------------------------------------------------------------------------
    // METODI PRIVATI (Helper)
    // -------------------------------------------------------------------------

    /**
     * Controlla se l'utente corrente ha il permesso di modificare/cancellare la ricetta.
     * Restituisce true se l'utente è l'autore della ricetta O se è un amministratore.
     */
    private boolean isAuthorized(Recipe recipe, User currentUser) {
        if (currentUser == null || recipe == null) {
            return false;
        }

        // Controllo Autore
        boolean isAuthor = recipe.getAuthor().getId().equals(currentUser.getId());

        // Controllo Admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Credentials.ADMIN_ROLE));

        return isAuthor || isAdmin;
    }
}