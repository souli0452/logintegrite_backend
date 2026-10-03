-- V11 : NIP (numero d'identification personnel) d'une personne physique.
-- Le formulaire de creation le proposait deja, mais le serveur l'ignorait : ce qui etait saisi etait perdu.
-- 17 caracteres (chiffres et lettres majuscules), facultatif, UNIQUE : deux personnes ne peuvent pas avoir le meme NIP.
ALTER TABLE personnes.personne_physique ADD COLUMN nip varchar(17);

ALTER TABLE personnes.personne_physique
    ADD CONSTRAINT ck_personne_physique_nip CHECK (nip IS NULL OR nip ~ '^[A-Z0-9]{17}$');

CREATE UNIQUE INDEX uq_personne_physique_nip ON personnes.personne_physique (nip) WHERE nip IS NOT NULL;
