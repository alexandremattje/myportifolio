create table app_user (
    id uuid primary key,
    email varchar(320) not null unique,
    display_name varchar(160) not null,
    created_at timestamptz not null default now()
);

create table portfolio (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    name varchar(160) not null,
    base_currency varchar(3) not null,
    created_at timestamptz not null default now()
);

create table asset (
    id uuid primary key,
    symbol varchar(32) not null,
    name varchar(200) not null,
    asset_type varchar(40) not null,
    market varchar(16) not null,
    currency varchar(3) not null,
    created_at timestamptz not null default now(),
    unique (symbol, market)
);

create table transaction_record (
    id uuid primary key,
    portfolio_id uuid not null references portfolio(id),
    asset_id uuid references asset(id),
    transaction_type varchar(40) not null,
    trade_date date not null,
    quantity numeric(24,10),
    unit_price numeric(24,10),
    fees numeric(24,10) not null default 0,
    taxes numeric(24,10) not null default 0,
    currency varchar(3) not null,
    notes varchar(1000),
    created_at timestamptz not null default now()
);

create index idx_portfolio_user on portfolio(user_id);
create index idx_transaction_portfolio_date on transaction_record(portfolio_id, trade_date);
