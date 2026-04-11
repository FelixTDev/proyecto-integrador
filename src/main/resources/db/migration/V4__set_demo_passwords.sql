UPDATE usuario
SET password_hash = '$2a$10$P671tvW64nmVSpr6nGgWruchBD.Kkdj.Axrceg0IU8/89N4cqUZTm',
    fecha_actualizacion = NOW()
WHERE email = 'admin@casachantilly.pe';

UPDATE usuario
SET password_hash = '$2a$10$p6HTl4Sgebs3Nm4uf2d2p.Y/kqyEKAhL35f.j836ATj9d4jppxWOy',
    fecha_actualizacion = NOW()
WHERE email = 'vendedor@casachantilly.pe';

UPDATE usuario
SET password_hash = '$2a$10$0pzOakC4Vy.jFgHZt3JR8.mzKtsa8Ifh08hpn3nfcFfZV8tkU/uV.',
    fecha_actualizacion = NOW()
WHERE email IN ('cliente1@casachantilly.pe', 'cliente2@casachantilly.pe', 'cliente3@casachantilly.pe');
