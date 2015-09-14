create table event_extension_types (
  id bigint auto_increment primary key,
  name varchar(256) not null,
  description varchar(256),
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
  );

create table event_extension_values (
  id bigint auto_increment primary key,
  event_id bigint not null references events on delete cascade,
  event_extension_type_id bigint not null references event_extension_types on delete cascade,
  value varchar(256),
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
  );
