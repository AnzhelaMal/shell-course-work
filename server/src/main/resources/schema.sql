create table if not exists app_state
(
    id                    bigserial primary key,
    left_panel_path       text        not null,
    right_panel_path      text        not null,
    active_panel_position varchar(10) not null,
    last_updated          timestamp   not null default current_timestamp
);

create index if not exists idx_app_state_last_updated on app_state (last_updated);

create table if not exists action_records
(
    id              bigserial primary key,
    event_type_name text      not null,
    timestamp       timestamp not null default current_timestamp,
    payload         JSONB     not null
);