package it.uniroma3.siw.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.IngredientService;
import it.uniroma3.siw.service.RecipeService;
import it.uniroma3.siw.service.ReviewService;
import it.uniroma3.siw.repository.UserRepository;
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
    
    @Autowired
    private UserRepository userRepository;

    // Blocca l'invio malevolo di campi sensibili tramite form
    //impedisce a un utente malintenzionato di iniettare un campo id o author nel form per sovrascrivere una ricetta 
    //di qualcun altro o farsi passare per un altro autore
    @InitBinder("recipe")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("author", "author.id", "user", "user.id", "id");
    }

    // -------------------------------------------------------------------------
    // SEZIONE PUBBLICA (Accessibile a tutti)
    // -------------------------------------------------------------------------

    @GetMapping("/recipe/{id}")
    public String getRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id); //recupera la ricetta 
        model.addAttribute("recipe", recipe);
 
        // Oggetti vuoti per i form nella pagina 
        model.addAttribute("ingredient", new Ingredient()); 
        model.addAttribute("review", new Review()); 
        
        return "recipe.html";
    }

    @GetMapping(value = {"/", "/recipes"})
    public String getRecipes(Model model) {     
        model.addAttribute("recipes", this.recipeService.findAll()); //carica tutte le ricette
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
         //da priorità al titolo
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


    //Pagina di creazione
    @GetMapping("/formNewRecipe")
    public String formNewRecipe(Model model) {
    	Recipe recipe = new Recipe();
    	// IMPOSTO IL DEFAULT A OGGI
    	recipe.setCreationDate(LocalDate.now());
        model.addAttribute("recipe", recipe);
        return "formNewRecipe.html";
    }

    //Salva la nuova ricetta
    @PostMapping("/formNewRecipe")
    public String newRecipe(@Valid @ModelAttribute("recipe") Recipe recipe, 
                            BindingResult bindingResult, 
                            Model model) {
        
        // Controllo utente loggato
        User currentUser = (User) model.getAttribute("currentUser");
        if (currentUser == null) 
        	return "redirect:/login";

        //  Validazione(es controlli se esiste già una ricetta con lo stesso nome)
        recipeValidator.validate(recipe, bindingResult);
        if (bindingResult.hasErrors()) {
            return "formNewRecipe"; 
        }
        
        //  Imposta i dati della ricetta (data e autore)
        recipe.setCreationDate(LocalDate.now());
        recipe.setAuthor(currentUser);

        
        // Il form ha riempito la lista 'recipe.ingredients', ma il campo 'recipe' 
        // dentro ogni ingrediente è ancora NULL. Dobbiamo settarlo ora.
        
        if (recipe.getIngredients() != null) {
            for (Ingredient ing : recipe.getIngredients()) { //prendi un ingrediente alla volta 
                ing.setRecipe(recipe); //la tua ricetta è X
               
            }
        }

        // 4. Salvataggio a Cascata (Salva ricetta + ingredienti insieme)
        //Salavo solo la ricetta, ma grazie a CascadeType.ALL (in recipe sulla lista ingredienti), salvando la madre (ricetta)
        //salvo tutti i figli( ingredienti)
        recipeService.save(recipe);
        
        return "redirect:/recipe/" + recipe.getId(); 
    }

    @PostMapping("/recipe/{recipeId}/review")
    public String addReview(@PathVariable("recipeId") Long recipeId,
                            @Valid @ModelAttribute("review") Review review,
                            BindingResult bindingResult, 
                            Model model) {
        //Recupero chi sta scrivendo e per quale ricetta
        User currentUser = (User) model.getAttribute("currentUser");
        Recipe recipe = recipeService.findById(recipeId);

        //controllo errore 
        if (bindingResult.hasErrors()) {
            model.addAttribute("recipe", recipe);
            model.addAttribute("ingredient", new Ingredient()); 
            return "recipe.html"; 
        }

        //salvataggio
        if (currentUser != null) {
            review.setUser(currentUser); //Questa recensione l'ha scritta X
            review.setRecipe(recipe); //Questa recensione è per la ricetta Y
            reviewService.save(review); 
        }
        //ricarico la pagina per far vedere il commento 
        return "redirect:/recipe/" + recipeId;
    }
    
    @GetMapping("/myRecipes")
    public String myRecipes(Model model) {
        // Usiamo l'utente già caricato dal GlobalController (funziona anche per Google)
        User currentUser = (User) model.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        //Query filtrata, in base all'autore 
        List<Recipe> recipes = recipeService.findByAuthor(currentUser);
        model.addAttribute("recipes", recipes);
        
        return "myRecipes.html"; 
    }
    
   
    // -------------------------------------------------------------------------
    // SEZIONE MODIFICA (Richiede Autore o Admin)
    // -------------------------------------------------------------------------
    
    //Aggiunta ingrediente a una ricetta già esistente nel DB
    //Relazione Uno-a-M (una ricetta->molti ingredienti)
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
       //creiamo un oggetto nuovo, copiando solo ciò che serve. In questo modo viene salvato nel DB un dato pulito 
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(ingredient.getName());
        newIngredient.setQuantity(ingredient.getQuantity());
        newIngredient.setUnit(ingredient.getUnit());
        
        //collega l'ingrediente alla ricetta nel database: questo ingrediente appartiene a questa ricetta specifica
         newIngredient.setRecipe(recipe);
         
       // this.ingredientService.save(newIngredient); ridondante, lo fa il cascade. Se salvo la ricetta, di conseguenza salvo tutti i nuovi figli 
        
        recipe.getIngredients().add(newIngredient); //aggiungi il nuovo ingrediente alla lista 
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

        //prendi la lista degli ingredienti e rimuovi se l'ID dell'ingrediente che stai guardando è uguale all'ID che vogliamo cancellare
        recipe.getIngredients().removeIf(ing -> ing.getId().equals(ingredientId));
        recipeService.save(recipe);

        return "redirect:/recipe/edit/" + recipeId;
    }    

    @PostMapping("/recipe/{id}/favorite")
    public String toggleFavorite(@PathVariable("id") Long id, 
                                 @RequestParam(value = "redirect", required = false) String redirect, //redirect->User Experience
                                 Model model) { 
        
        User currentUser = (User) model.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        Recipe recipe = recipeService.findById(id);
        if (recipe != null) {
        	//logica interruttore 
            if (currentUser.getFavoriteRecipes().contains(recipe)) { //controlla se quella ricetta è già nella lista prefe dell'utente 
                currentUser.getFavoriteRecipes().remove(recipe);
            } else {
                currentUser.getFavoriteRecipes().add(recipe);
            }
            userRepository.save(currentUser); //salviamo l'utente, perchè la lista dei prefe è una proprietà dell'utente(User ha una lista (favoriteRecipes)
        }                                     //aggiornando l'utente, il DB aggiorna la tabella di collegamente (M-T-M) tra utenti e ricette 
        
        // Controllo: se il parametro redirect che arriva dall'URl(?redirect=profile) 
        if ("profile".equals(redirect)) {
        	//(costruisce l'indirizzo (/user/5)
            return "redirect:/user/" + currentUser.getId();
        }
        
        // Altrimenti (se clicchi dalla home) torna alla lista ricette
        return "redirect:/recipes"; 
    }
    
    //Prepara il form per modificare la ricetta
    @GetMapping("/recipe/edit/{id}") //legge che ricetta vogliamo modificare dall 'URL
    public String editRecipe(@PathVariable("id") Long id, Model model) {
        Recipe recipe = recipeService.findById(id); //prende in archivio la ricetta
        User currentUser = (User) model.getAttribute("currentUser"); //vediamo chi è l'utente corrente

        //devi essere l'autore o Admin
        if (!isAuthorized(recipe, currentUser)) {
            return "redirect:/recipes?error=notAuthorized";
        }

        model.addAttribute("recipe", recipe); //vecchia ricetta da modificare nel modello 
        model.addAttribute("ingredient", new Ingredient());
        return "editRecipe.html"; 
    }
    
    //salvataggio modifiche (fonde i dati nuovi che arrivano dal form con i dati del DB)
    @PostMapping("/recipe/update/{id}")
    public String updateRecipe(@PathVariable("id") Long id,
                               @ModelAttribute("recipe") Recipe formRecipe,
                               BindingResult bindingResult,
                               Model model) {

        User currentUser = (User) model.getAttribute("currentUser");
        Recipe recipeInDb = recipeService.findById(id); //se esiste, recuperi la ricetta originale dal database(recipeInDb)
        
        if (recipeInDb == null) 
        	return "redirect:/recipes";

        if (!isAuthorized(recipeInDb, currentUser)) {
            return "redirect:/recipes?error=notAuthorized";
        }

        // Prepariamo l'oggetto per la validazione
        //metto l'ID così capisce che è la ricetta vecchia, e non un duplicato
        formRecipe.setId(id);
     
        formRecipe.setIngredients(recipeInDb.getIngredients()); // Manteniamo gli ingredienti esistenti
        //controlla se ho fatto errori
        this.recipeValidator.validate(formRecipe, bindingResult);
        
        if (bindingResult.hasErrors()) {
            // Trick per visualizzare l'autore nel template anche in caso di errore
        	//creo una copia temporanea dell autore (dummyAuthor)
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
       
       //Aggiorniamo solo cià che è permesso modificare, altrimenti salvando formRecipe, perderemo quei dati che non vengono gestisci 
        //come id, autore, data, recensioni
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

        recipeService.save(recipeInDb); //abbiamo messo l'ID all'inzio, questo metodo .save() capisce che deve fare un UPDATE

        return "redirect:/recipe/" + id;
    }

       
    @PostMapping("/recipe/delete/{id}") 
    public String deleteRecipe(@PathVariable("id") Long id, Model model) {
        
        Recipe recipe = recipeService.findById(id);
        User currentUser = (User) model.getAttribute("currentUser");

        if (recipe == null) 
            return "redirect:/recipes";

        // Questo controllo verifica se sei l'autore OPPURE un Admin
        if (isAuthorized(recipe, currentUser)) {
            //cancellazione
            recipeService.deleteById(id); //con cascade cancella la ricetta,i suoi ingredienti, le recensioni associate, i preferiri 
            
            //Dobbiamo decidere dove mandare l'utente dopo aver cancellato 
            // Recuperiamo i permessi dell'utente loggato
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Controlliamo se tra i permessi c'è "ADMIN"
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));
            
            if (isAdmin) {
                // Se sei l'Admin torni al pannello di controllo)
                return "redirect:/admin/manageRecipes/all"; 
            } else {
                // Se sei l'utente normale, torni alla tua bacheca personale
                return "redirect:/myRecipes"; 
            }
  
         } else {
            return "redirect:/recipe/" + id + "?error=notAuthorized";
        }
    }
    
    // -------------------------------------------------------------------------
    // SEZIONE ADMIN (Generale)
    // -------------------------------------------------------------------------

    @GetMapping("/admin/indexAdmin")
    public String manageRecipes(Model model) {
        model.addAttribute("recipes", recipeService.findAll());
        return "admin/indexAdmin.html"; 
    }
    
    @GetMapping("/admin/manageRecipes/all")
    @PreAuthorize("hasAuthority('ADMIN')") // Protegge il portale: solo Admin possono entrare
    //principal è un oggetto standard di java security, rappresenta chi è loggato in quel momento 
    public String allRecipes(Model model) {
        model.addAttribute("recipes", recipeService.findAll()); //vede tutte le ricette 
       // model.addAttribute("viewTitle", "Gestione Ricette"); 
      
        return "admin/manageRecipes";
    }

    // -------------------------------------------------------------------------
    // METODI PRIVATI (Helper)
    // -------------------------------------------------------------------------

    /**
     * Controlla se l'utente corrente ha il permesso di modificare/cancellare la ricetta.
     * Restituisce true se l'utente è l'autore della ricetta o se è un admin
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