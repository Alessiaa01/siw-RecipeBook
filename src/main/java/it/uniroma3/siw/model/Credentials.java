package it.uniroma3.siw.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import jakarta.persistence.Column; // Importa @Column
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Table;

@Entity
@Table(name = "credentials")
public class Credentials {

	public static final String DEFAULT_ROLE = "DEFAULT";
	public static final String ADMIN_ROLE = "ADMIN";
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NotBlank
    @Column(unique = true) 
	private String username;
	private String password;
	private String role;

	@OneToOne(cascade = CascadeType.ALL)
	private User user;
	
	// true = attivo, false = bannato
    private boolean enabled = true;
	
	public String getUsername() {
		return username;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
