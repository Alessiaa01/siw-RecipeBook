-- Inserisci l'utente. Il DB assegnerà automaticamente id=1
insert into users(name, surname, email) values('alessia', 'occhionero', 'alessia@gmail.com');

-- Inserisci le credenziali. Il DB assegnerà automaticamente id=1
-- Noi impostiamo manualmente user_id=1 per collegarlo all'utente 'paolo' appena creato
insert into credentials(password, role, username, user_id,enabled) values('$2a$12$jxOF7FG6Hdjkv6OzdDNlnOedw4igAdVIVXqTO.r9wcP1pgyxdaiXa', 'ADMIN', 'alessia', 1,true);