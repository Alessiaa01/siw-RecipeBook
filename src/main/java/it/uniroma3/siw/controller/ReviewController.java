package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.ReviewService;
// Assicurati di importare anche User se serve

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CredentialsService credentialsService;

    // --- CANCELLAZIONE ---
    @GetMapping("/review/delete/{id}")
    public String deleteReview(@PathVariable("id") Long id) {
        Review review = reviewService.findById(id);
        
        // Recupera utente corrente
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());

        // Controllo Permessi: Elimina solo se sei l'autore o se sei ADMIN
        if (review.getUser().equals(credentials.getUser()) || 
            credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            
            Long recipeId = review.getRecipe().getId(); // Ci serve per tornare alla pagina giusta
            reviewService.deleteById(id);
            return "redirect:/recipe/" + recipeId;
        }
        
        // Se non autorizzato, rimanda alla ricetta (o pagina errore)
        return "redirect:/recipes";
    }

    // --- MODIFICA (GET - Mostra Form) ---
    @GetMapping("/review/edit/{id}")
    public String editReviewForm(@PathVariable("id") Long id, Model model) {
        Review review = reviewService.findById(id);
        
        // Controllo Permessi (Simile a sopra)
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());

        if (!review.getUser().equals(credentials.getUser()) && 
            !credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            return "redirect:/recipe/" + review.getRecipe().getId();
        }

        model.addAttribute("review", review);
        return "formEditReview.html"; // Creeremo questa pagina tra poco
    }

    // --- MODIFICA (POST - Salva Dati) ---
    @PostMapping("/review/update/{id}")
    public String updateReview(@PathVariable("id") Long id, @ModelAttribute("review") Review reviewDetails) {
        Review review = reviewService.findById(id);
        
        // Qui dovresti ripetere il controllo permessi per sicurezza
        
        // Aggiorna i campi
        review.setTitle(reviewDetails.getTitle());
        review.setText(reviewDetails.getText());
        review.setRating(reviewDetails.getRating());
        
        reviewService.save(review);
        
        return "redirect:/recipe/" + review.getRecipe().getId();
    }
}