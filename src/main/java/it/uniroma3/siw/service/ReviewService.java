package it.uniroma3.siw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Review;
import it.uniroma3.siw.repository.ReviewRepository;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Salva una nuova recensione nel database.
     * L'uso di @Transactional garantisce l'integrità del dato.
     */
    @Transactional
    public void save(Review review) {
        reviewRepository.save(review);
    }

    /**
     * Recupera tutte le recensioni associate a una ricetta tramite il suo ID.
     */
    @Transactional(readOnly = true)
    public List<Review> findByRecipeId(Long recipeId) {
        return reviewRepository.findByRecipeId(recipeId);
    }

    /**
     * Elimina una recensione specifica tramite ID.
     */
    @Transactional
    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }
}