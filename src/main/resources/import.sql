-- Inserisci l'utente con la data di nascita (formato 'AAAA-MM-GG')
INSERT INTO users (id,name, surname, email, date_of_birth) VALUES (1,'alessia', 'occhionero', 'alessia@gmail.com', '2001-03-27');

-- Inserisci le credenziali (collegando user_id=1)
INSERT INTO credentials (password, role, username, user_id, enabled) VALUES ('$2a$12$AMC0tDbfcTEOKQd3BizQ3e1Ez5JLajLAIskHZt...4PORrJ8mRHd2', 'ADMIN', 'alessia', 1, true);

ALTER SEQUENCE users_seq RESTART WITH 100;