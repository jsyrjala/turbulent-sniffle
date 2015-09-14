create table users (
  id bigint auto_increment primary key,
  username varchar(256) not null,
  password_hash varchar(256),
  name varchar(128),
  email varchar(256),
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
  );

create unique index uix_users_username on users(username);
