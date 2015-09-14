create table event_sessions (
  id bigint auto_increment primary key,
  tracker_id bigint not null references trackers on delete cascade,
  latest_event_time timestamp,
  first_event_time timestamp,
  session_code varchar(50) not null,
  updated_at timestamp not null default now(),
  created_at timestamp not null default now()
);

create unique index uix_event_sessions_code on event_sessions (session_code, tracker_id);
create index ix_event_sessions_latest on event_sessions (latest_event_time);
