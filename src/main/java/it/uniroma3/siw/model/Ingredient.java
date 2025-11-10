package it.uniroma3.siw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
//import jakarta.persistence.JoinColumn;

import java.util.Objects;

@Entity
public class Ingredient {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

   // private String quantity; // opzionale, se vuoi specificare quantità tipo "100g", "2 cucchiai", ecc.

    @ManyToOne
    //@JoinColumn(name = "recipe_id")
    private Recipe recipe;


    // --- Getters e Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
/*
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
*/
    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

//--- equals e hashCode basati sul nome ---
@Override
public int hashCode() {
    return Objects.hash(name);
}

@Override
public boolean equals(Object obj) {
    if (this == obj)
        return true;
    if (obj == null || getClass() != obj.getClass())
        return false;
    Ingredient other = (Ingredient) obj;
    return Objects.equals(name, other.name);
}
}