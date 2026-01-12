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

import static it.uniroma3.siw.model.Credentials.ADMIN_ROLE;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
//public  class WebSecurityConfig {
	public class AuthConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .authoritiesByUsernameQuery("SELECT username, role from credentials WHERE username=?")
                .usersByUsernameQuery("SELECT username, password, enabled FROM credentials WHERE username=?");
    }
    
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf().and().cors().disable()
                .authorizeHttpRequests()
                
                // --- 1. PAGINE APERTE A TUTTI (Pubbliche) ---
                .requestMatchers(HttpMethod.GET,
                        "/", "/index", 
                        "/register", "/login", 
                        "/css/**", "/images/**", "favicon.ico", 
                        "/recipes",       // Elenco ricette visibile a tutti
                        "/recipe/**", 
                        "/searchRecipes",// Dettaglio ricetta visibile a tutti
                        "/cooks", "/cook/**" // Se hai i cuochi
                ).permitAll()
                
                // Login e Register (Invio dati) sono pubblici
                .requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
                
                // --- 2. PAGINE SOLO PER L'AMMINISTRATORE ---
                // Qualsiasi URL che inizia con /admin/ richiede il ruolo ADMIN
                .requestMatchers(HttpMethod.GET, "/admin/**").hasAnyAuthority(ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, "/admin/**").hasAnyAuthority(ADMIN_ROLE)
                
                // --- 3. PAGINE PER GLI UTENTI LOGGATI ---
                // Qui ricadono AUTOMATICAMENTE:
                // - /formNewRecipe (perché non è pubblica e non è admin)
                // - /myRecipes
                // - Salvare una ricetta
                .anyRequest().authenticated()
                
                // --- CONFIGURAZIONE LOGIN ---
                .and().formLogin()
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/success", true)
                .failureUrl("/login?error=true")
                
                // --- CONFIGURAZIONE LOGOUT ---
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .clearAuthentication(true).permitAll();
        
        return httpSecurity.build();
    }
}