package it.uniroma3.siw.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import it.uniroma3.siw.authentication.OAuth2LoginSuccessHandler;

import static it.uniroma3.siw.model.Credentials.ADMIN_ROLE;

import javax.sql.DataSource;

@Configuration //impostazioni e config da caricare all'avvio 
@EnableWebSecurity //senza le regole sotto verrebbero ignorate
public class AuthConfiguration {

	//dataSource collega l'applicazione java al database
	//Spring Security ne ha bisogno per andare a leggere le tabelle degli utenti.
    @Autowired
    private DataSource dataSource;

    //Per gestire chi entra tramite google
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    //controllo se username e psw sono giusti 
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    	//Usa il DB per l'autenticazione
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                //controllo ruolo
                .authoritiesByUsernameQuery("SELECT username, role from credentials WHERE username=?")
                //controllo identità
                .usersByUsernameQuery("SELECT username, password, enabled FROM credentials WHERE username=?");
    }
    
    //usa l'algoritmo BCrypt. Usato sia per criptare le password nuove, 
    //sia per controllare quelle di chi sta facendo il login
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //Per fare il login automatico post-registrazione
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    //dove puoi andare una volta entrato 
    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
        //protezioni standard contro gli attacchi hacker(in questo caso non ci servono)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                //ordine fondamentale!
                //Decidi chi può vedere cosa 
                .authorizeHttpRequests(auth -> auth
                		//CHIUNQUE può vedere la pagina 
                    .requestMatchers(HttpMethod.GET, "/", "/index", "/register", "/login", "/css/**", "/images/**", "favicon.ico", "/recipes", "/recipe/**", "/searchRecipes", "/cooks", "/cook/**").permitAll()
                    // CHIUNQUE deve poter inviare i dati per registrarsi o loggarsi
                    .requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
                    .requestMatchers("/user/**").permitAll()
                    //Se L'URL inizia con /admin/ puoi entrare 
                    .requestMatchers("/admin/**").hasAnyAuthority(ADMIN_ROLE)
                    //per tutte le altre pagine non menzionate sopra l'utente deve essere per forza loggato per poterci andare
                    .anyRequest().authenticated()
                )
                
                .formLogin(form -> form
                    .loginPage("/login") //la pagina con il form
                    .defaultSuccessUrl("/success", true) //se entra, vai qui 
                    .failureUrl("/login?error=true") // se sbaglia, ricarica la pagina con un errore
                )
                //Login speciale con google
                .oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                      //Quando google dice ok è Mario, dobbiamo intercettare qwuel momento per salvare Mario anche nel nostro DB locale
                    .successHandler(oAuth2LoginSuccessHandler) 
                )
                
                //serve per assicurarsi di non poter tornare indietro dopo aver fatto il logout
                .logout(logout -> logout
                    .logoutUrl("/logout") //indirizzo per uscire 
                    .logoutSuccessUrl("/") //dopo torna alla home
                    .invalidateHttpSession(true) //cancella la memoria del server 
                    .deleteCookies("JSESSIONID") //Quando fai il login, il server (Java) dà al tuo browser un Cookie con un codice lungo. 
                    //Ogni volta che cambi pagina, il browser mostra questo codice al server per dire "Sono sempre io, Mario".
                    //Conferma a Spring Security che l'azione di logout deve scattare esattamente quando viene chiamata l'URL /logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    //assicura che quell'oggetto venga cancellato istantaneamente dalla memoria del server
                    .clearAuthentication(true).permitAll() //chiunque può accedere alla funzione di logout 
                );
        return httpSecurity.build(); //tutte le regole diventano un oggetto reale e funzionante 
    }
}