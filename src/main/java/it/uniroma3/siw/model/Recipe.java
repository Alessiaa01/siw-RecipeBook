package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;

import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import jakarta.persistence.CascadeType;


// Non sono necessari altri import per gli attributi semplici richiesti

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // Identificatore unico della ricetta

    private String title; // Titolo della ricetta
    
    private String description; // Descrizione e/o procedimento
    
    private Integer preparationTime; // Tempo di preparazione (in minuti)

    // Difficoltà potrebbe essere una stringa (es. "Facile", "Medio") o un Integer (es. da 1 a 5)
    private String difficulty; 

    private String imageUrl; // URL per l'immagine della ricetta
    
    private Integer servings; // Numero di porzioni
    
    @Column(length = 5000) 
    private String procedure; //procedimento
    
    private String category; //categoria es antipasto, primo, secondo , dolce
    

    // ---------------------------------------------------------------------------------
    // Costruttori, Getter, Setter, equals e hashCode
    // ---------------------------------------------------------------------------------

    // Costruttore vuoto (richiesto da JPA)
    public Recipe() {
    }

    // Costruttore con attributi (opzionale, ma utile)
    public Recipe(String title, String description, Integer preparationTime, String difficulty, String imageUrl, Integer servings) {
        this.title = title;
        this.description = description;
        this.preparationTime = preparationTime;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.servings = servings;
    }

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    
    
    @ElementCollection
    private Set<String> tags = new HashSet<>();

   


    // --- Getter e Setter ---

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(Integer preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }
    
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category =  category;
    }
    // --- Metodi equals e hashCode (importanti per la persistenza e il confronto) ---
    // Due oggetti Recipe sono considerati uguali se hanno lo stesso titolo.
    // In un sistema reale, dovresti considerare anche l'Utente che l'ha creata
    // per garantire unicità (e.g., stesso titolo MA chef diversi sono ok)
    // Per ora ci limitiamo a 'title' e 'description'.

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Recipe other = (Recipe) obj;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        return true;
    }
}