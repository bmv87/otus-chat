INSERT INTO roles (role_id, name, description) VALUES ('514B0EA1-5502-4DE9-A030-3E1D6FFA10AF','USER','Пользователь');
INSERT INTO roles (role_id, name, description) VALUES ('2BE95580-A873-42F3-9C0F-B47B080CF946','ADMIN','Администратор');
INSERT INTO users (user_id,login,password,role_id,username) VALUES ('016FABE9-9855-4128-900D-ED97C91A487E','Admin1','password1','2BE95580-A873-42F3-9C0F-B47B080CF946','Bob');
INSERT INTO users (user_id,login,password,role_id,username) VALUES ('CC5F370E-526D-4FBE-88D1-221B1F690661','User1','password3','514B0EA1-5502-4DE9-A030-3E1D6FFA10AF','Vasya');
INSERT INTO users (user_id,login,password,role_id,username) VALUES ('265E51C8-9C87-404F-99FC-0E784697ED9F','User2','password4','514B0EA1-5502-4DE9-A030-3E1D6FFA10AF','Petya');
INSERT INTO users (user_id,login,password,role_id,username) VALUES ('C5B22640-83AC-4560-ACFA-BACD25B1595F','User3','password5','514B0EA1-5502-4DE9-A030-3E1D6FFA10AF','Tanya');
