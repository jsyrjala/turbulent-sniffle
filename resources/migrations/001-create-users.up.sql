create table users (
  id bigint auto_increment primary key,
  username varchar(256) not null,
  password_hash varchar(256),
  name varchar(128),
  email varchar(256),
  last_login timestamp,
  prev_login timestamp,
  last_failed_login timestamp,
  failed_login_count integer not null default 0,
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
  );

create unique index uix_users_username on users(username);
create unique index uix_users_email on users(email);
create index ix_users_username_email on users(username,email);
