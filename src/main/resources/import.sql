-- Inserisci l'utente. Il DB assegnerà automaticamente id=1
insert into users(name, surname, email) values('paolo', 'paolo', 'paolo@gmail.com');

-- Inserisci le credenziali. Il DB assegnerà automaticamente id=1
-- Noi impostiamo manualmente user_id=1 per collegarlo all'utente 'paolo' appena creato
insert into credentials(password, role, username, user_id) values('$2a$10$F605H0XOC06ODyI.oQnCzeMWpWRPNOH2DM2Lmf.ZSAkNrTG6Kqa3q', 'ADMIN', 'paolo', 1);