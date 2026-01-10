package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String title; // Titolo sintetico della recensione

    @Column(length = 1000)
    @NotBlank
    private String text; // Il contenuto testuale del commento

    @Min(1)
    @Max(5)
    private Integer rating; // Valutazione da 1 a 5 stelle

    @ManyToOne
    private Recipe recipe; // La ricetta a cui appartiene la recensione

    @ManyToOne
    private User user; // L'utente (Chef) che ha scritto la recensione

    // ---------------------------------------------------------------------------------
    // Costruttori
    // ---------------------------------------------------------------------------------

    public Review() {
    }

    public Review(String title, String text, Integer rating, Recipe recipe, User user) {
        this.title = title;
        this.text = text;
        this.rating = rating;
        this.recipe = recipe;
        this.user = user;
    }

    // ---------------------------------------------------------------------------------
    // Getter e Setter
    // ---------------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // ---------------------------------------------------------------------------------
    // Equals e HashCode
    // ---------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        // Una recensione è considerata duplicata se scritta dallo stesso utente per la stessa ricetta
        return Objects.equals(recipe, review.recipe) && 
               Objects.equals(user, review.user) && 
               Objects.equals(title, review.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, recipe, user);
    }
}