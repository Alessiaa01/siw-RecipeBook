package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ReviewService;
import jakarta.validation.Valid;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // --- CANCELLAZIONE ---
    @GetMapping("/review/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id, Model model) {
        Review review = reviewService.findById(id);
        User currentUser = (User) model.getAttribute("currentUser");

       
        if (isAuthorized(review, currentUser)) {
        	//salviamo l?ID della ricetta prima di cancellare la recensione , altrimenti non sapremo dove ritornare 
            Long recipeId = review.getRecipe().getId();
            reviewService.deleteById(id);
            return "redirect:/recipe/" + recipeId;
        }
        
        if (review != null) {
            return "redirect:/recipe/" + review.getRecipe().getId() + "?error=notAuthorized";
        }
        return "redirect:/recipes";
    }

 // --- Mostra il form (Form inline nella pagina ricetta) ---
    @GetMapping("/review/edit/{id}")
    public String editReviewForm(@PathVariable("id") Long id, Model model) {
        Review review = reviewService.findById(id);
        User currentUser = (User) model.getAttribute("currentUser");

        if (!isAuthorized(review, currentUser)) {
            Long recipeId = (review != null) ? review.getRecipe().getId() : null;
            return "redirect:/recipe/" + (recipeId != null ? recipeId : "") + "?error=notAuthorized";
        }

        // Carichiamo i dati necessari per "recipe.html"
        model.addAttribute("recipe", review.getRecipe());
        model.addAttribute("reviewToEdit", review); // Questa è la recensione da modificare(con i dati vecchi)
        
        // Dobbiamo aggiungere anche gli oggetti vuoti che il template recipe.html si aspetta normalmente, per non crashare 
        model.addAttribute("review", new Review()); 
        model.addAttribute("ingredient", new it.uniroma3.siw.model.Ingredient());

        return "recipe.html"; // Ritorniamo il template della ricetta
    }
    
    @PostMapping("/review/update/{id}")
    public String updateReview(@PathVariable("id") Long id, 
                               @Valid @ModelAttribute("reviewToEdit") Review reviewDetails, // deve avere lo stesso nome che abbiamo usato nel metodo GET precedente.
                               //Così Spring capisce che stiamo parlando dello stesso oggetto.
                               BindingResult bindingResult,
                               Model model) {

        Review reviewInDb = reviewService.findById(id); //recemsione originale dal DB
        User currentUser = (User) model.getAttribute("currentUser");

        if (!isAuthorized(reviewInDb, currentUser)) {
             return "redirect:/recipes?error=notAuthorized";
        }

        if (bindingResult.hasErrors()) {
            // Se c'è un errore, riempiamo il model con tutto ciò che serve alla pagina 
            model.addAttribute("recipe", reviewInDb.getRecipe());
            model.addAttribute("reviewToEdit", reviewDetails);
            model.addAttribute("review", new Review());
            model.addAttribute("ingredient", new it.uniroma3.siw.model.Ingredient());
            return "recipe.html";
        }

        // Aggiornamento dei dati
        reviewInDb.setTitle(reviewDetails.getTitle());
        reviewInDb.setText(reviewDetails.getText());
        reviewInDb.setRating(reviewDetails.getRating());
        
        reviewService.save(reviewInDb);
        return "redirect:/recipe/" + reviewInDb.getRecipe().getId();
    }

    // -------------------------------------------------------------------------
    // METODO PRIVATO (Helper)
    // -------------------------------------------------------------------------
    
    private boolean isAuthorized(Review review, User currentUser) {
        if (review == null || currentUser == null) {
            return false;
        }

        // 1. È l'autore della recensione?
        boolean isAuthor = review.getUser().getId().equals(currentUser.getId());

        // 2. È un Admin?
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Credentials.ADMIN_ROLE));

        return isAuthor || isAdmin;
    }
}